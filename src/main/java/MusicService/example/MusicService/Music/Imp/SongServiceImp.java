package MusicService.example.MusicService.Music.Imp;

import MusicService.example.MusicService.Music.S3Services;
import MusicService.example.MusicService.Music.Song;
import MusicService.example.MusicService.Music.SongRepository;
import MusicService.example.MusicService.Music.SongService;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.TikaException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SongServiceImp implements SongService {


    private final SongRepository songRepository;
    private final S3Services s3Services;

    @Autowired
    public SongServiceImp(SongRepository songRepository, S3Services s3Services) {
        this.songRepository = songRepository;
        this.s3Services = s3Services;
    }

    @Transactional
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
        Song savedSong = songRepository.save(song);
        songRepository.flush();
        return savedSong;
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

    @Override
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

}