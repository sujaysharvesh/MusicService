package MusicCloud.example.MusicService.Music.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SongResponseDTO {
    private UUID id;
    private String title;
    private String s3Key;
    private String contendType;
    private int durationSec;
    private Long fileSize;

    public SongResponseDTO(String songNotFound) {
    }

    public SongResponseDTO(String title, InputStream audioStream, long contentLength) {
    }

    public SongResponseDTO(UUID id, String title, String s3Key, String contentType, int durationSec, Long fileSize, Object o) {
    }
}
