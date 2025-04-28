package MusicService.example.MusicService.Music.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
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


    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }


    // Constructor for success responses
    public SongUploadResponseDTO(UUID id, UUID id1, String s3Key, String title, Long fileSize, int durationSec, String contentType, Object o, String uploadedAt) {
    }

    // Constructor for error responses
    public SongUploadResponseDTO(String errMessage) {
        this.errMessage = errMessage;
        this.id = null;
        this.userId = null;
        this.s3Key = null;
        this.title = null;
        this.fileSize = null;
        this.durationSec = 0;
        this.contentType = null;
        this.uploadedAt = null;
    }
}
