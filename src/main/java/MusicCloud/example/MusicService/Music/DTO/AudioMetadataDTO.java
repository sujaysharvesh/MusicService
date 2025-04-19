package MusicCloud.example.MusicService.Music.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AudioMetadataDTO {
    private final String title;
    private final String artist;
    private final int duration;

    public void setDuration(long durationInSeconds) {
    }
}
