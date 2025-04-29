package MusicService.example.MusicService.Music.DTO;

import MusicService.example.MusicService.Music.Song;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class SongUploadResponseDTO {
    private UUID id;
    private UUID userId;
    private String s3Key;
    private String title;
    private Long fileSize;
    private int durationSec;
    private String contentType;
    private String errMessage;
    private String uploadedAt;

    // Default constructor
    public SongUploadResponseDTO() {
    }

    // Constructor with error message
    public SongUploadResponseDTO(String errMessage) {
        this.errMessage = errMessage;
    }

    // Constructor from Song entity
    public SongUploadResponseDTO(Song song, UUID userId) {
        if (song != null) {
            this.id = song.getId();
            this.userId = userId;
            this.s3Key = song.getS3Key();
            this.title = song.getTitle();
            this.fileSize = song.getFileSize();
            this.durationSec = song.getDurationSec();
            this.contentType = song.getContentType();
            this.uploadedAt = song.getUploadedAt();
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return "SongUploadResponseDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", s3Key='" + s3Key + '\'' +
                ", title='" + title + '\'' +
                ", fileSize=" + fileSize +
                ", durationSec=" + durationSec +
                ", contentType='" + contentType + '\'' +
                ", errMessage='" + errMessage + '\'' +
                ", uploadedAt='" + uploadedAt + '\'' +
                '}';
    }
}