package MusicCloud.example.MusicService.Music;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Songs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
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

}
