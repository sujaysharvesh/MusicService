package MusicService.example.MusicService.Music.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;
import java.util.UUID;

@Getter
@Setter
public class SongResponseDTO {
    private UUID id;
    private String title;
    private String s3Key;
    private String contendType;
    private int durationSec;
    private Long fileSize;

    public SongResponseDTO(UUID id, String title, String s3Key, String contentType, int durationSec, Long fileSize) {
    }

    public String getContendType() {
        return contendType;
    }

    public void setContendType(String contendType) {
        this.contendType = contendType;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
