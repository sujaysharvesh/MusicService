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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.File;
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
            throws IOException, TikaException, SAXException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotReadException {

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

        // 4. Try extracting more accurate duration using jaudiotagger
        File tempFile = File.createTempFile("upload", "." + fileExtension);
        file.transferTo(tempFile);
        AudioFile audioFile = AudioFileIO.read(tempFile);
        int durationInSeconds = audioFile.getAudioHeader().getTrackLength();
        String title = audioFile.getTag().getFirst(FieldKey.TITLE);
        tempFile.delete(); // Clean up temp file
        // 4. Create and save song
        Song song = new Song();
        song.setUserId(userId);
        song.setS3Key(s3Key);
        song.setTitle(title);
        song.setDurationSec(durationInSeconds);
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

    @Override
    public void deleteSong(UUID songId) {
        songRepository.findById(songId)
                .ifPresent(song -> {
                    s3Services.deleteFile(song.getS3Key());
                    songRepository.delete(song);
                });
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
                (int) parseDuration(metadata.get("xmpDM:duration"))
        );
    }
    private long parseDuration(String durationStr) {
        if (durationStr != null) {
            return (long) (Double.parseDouble(durationStr) / 1000); // Convert ms to seconds
        }
        return 0; // Default duration if not available
    }
}