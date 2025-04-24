package MusicService.example.MusicService.Music;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Songs")
@Getter @Setter
public class Song {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = false, name = "userId")
    private UUID userId;

    @Column(nullable = false, unique = true, name = "S3key")
    private String s3Key;

    @Column(nullable = false, unique = false, name = "title")
    private String title;

    @Column(nullable = false, unique = false, name = "fileSize")
    private Long fileSize;

    @Column(nullable = false, unique = false, name = "durationSec")
    private int durationSec;

    @Column(nullable = false, unique = false, name = "contentType")
    private String contentType;

    @Column(nullable = false, name = "createdAt")
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

}
