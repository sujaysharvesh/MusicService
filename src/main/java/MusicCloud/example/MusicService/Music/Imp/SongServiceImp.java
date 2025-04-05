package MusicCloud.example.MusicService.Music.Imp;

import MusicCloud.example.MusicService.Music.DTO.AudioMetadataDTO;
import MusicCloud.example.MusicService.Music.S3Services;
import MusicCloud.example.MusicService.Music.Song;
import MusicCloud.example.MusicService.Music.SongReponsitory;
import MusicCloud.example.MusicService.Music.SongService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SongServiceImp implements SongService {

    private final SongReponsitory songRepository;
    private final S3Services s3Services;

    @Override
    public Song uploadSong(MultipartFile file, UUID userId)
            throws IOException, TikaException, SAXException {

        // 1. Generate safe S3 key
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        String s3Key = String.format("users/%s/songs/%s.%s",
                userId,
                UUID.randomUUID(),
                fileExtension.toLowerCase());

        // 2. Upload to S3
        s3Services.uploadFile(
                s3Key,
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        // 3. Extract metadata
        AudioMetadataDTO metadata = extractMetadata(file);

        // 4. Create and save song
        Song song = new Song();
        song.setUserId(userId);
        song.setS3Key(s3Key);
        song.setTitle(metadata.getTitle());
        song.setDurationSec(metadata.getDuration());
        song.setFileSize(file.getSize());
        song.setContentType(file.getContentType());
        song.setUploadedAt(String.valueOf(Instant.now()));

        return songRepository.save(song);
    }

    @Override
    public List<Song> getUserSongs(UUID userId) {
        return songRepository.getAllSongsByUserId(userId);
    }

    @Override
    public Song getSongById(UUID songId) {
        return songRepository.findById(songId).orElseThrow(() -> new RuntimeException("Song not found"));
    }


    private AudioMetadataDTO extractMetadata(MultipartFile file)
            throws IOException, TikaException, SAXException {

        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (InputStream stream = file.getInputStream()) {
            parser.parse(stream, handler, metadata, context);
        }

        return new AudioMetadataDTO(
                Optional.ofNullable(metadata.get("title"))
                        .orElse(FilenameUtils.getBaseName(file.getOriginalFilename())),
                Optional.ofNullable(metadata.get("xmpDM:artist"))
                        .orElse("Unknown Artist"),
                parseDuration(metadata.get("xmpDM:duration"))
        );
    }

    private int parseDuration(String durationStr) {
        if (durationStr == null) return 0;
        try {
            return (int) (Double.parseDouble(durationStr) * 1000);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}