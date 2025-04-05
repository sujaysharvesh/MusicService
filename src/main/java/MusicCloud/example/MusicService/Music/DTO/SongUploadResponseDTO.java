package MusicCloud.example.MusicService.Music.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    // Constructor for success responses
    public SongUploadResponseDTO(UUID id, UUID userId, String s3Key, String title, Long fileSize, int durationSec, String contentType, String uploadedAt) {
        this.id = id;
        this.userId = userId;
        this.s3Key = s3Key;
        this.title = title;
        this.fileSize = fileSize;
        this.durationSec = durationSec;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
        this.errMessage = null;  // No error in success case
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
