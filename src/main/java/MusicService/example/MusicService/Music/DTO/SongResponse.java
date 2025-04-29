package MusicService.example.MusicService.Music.DTO;

import MusicService.example.MusicService.Music.Song;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class SongResponse {
    private UUID id;
    private String title;
    private String s3Key;
    private String contentType;
    private Integer durationSec;
    private Long fileSize;

    public SongResponse(Song song) {
        if (song != null) {
            this.id = song.getId();
            this.title = song.getTitle();
            this.s3Key = song.getS3Key();
            this.contentType = song.getContentType();
            this.durationSec = song.getDurationSec();
            this.fileSize = song.getFileSize();
        }
    }
}