package MusicCloud.example.MusicService.Music;


import org.apache.tika.exception.TikaException;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(transactionManager = "musicTransactionManager")
public interface SongService {
    Song uploadSong(MultipartFile file, UUID userId) throws IOException, TikaException, SAXException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotReadException;
    List<Song> getUserSongs(UUID userId);
    Song getSongById(UUID songId);
    void deleteSong(UUID songId);
}
