package MusicCloud.example.MusicService.Music.ErrorHandler;


import MusicCloud.example.MusicService.Music.DTO.SongUploadResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<SongUploadResponseDTO> handleUploadError(MultipartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SongUploadResponseDTO("File upload failed: " + ex.getMessage()));
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<SongUploadResponseDTO> handleS3Error(S3Exception ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new SongUploadResponseDTO("Storage service unavailable"));
    }
}