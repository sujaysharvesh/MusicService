package MusicService.example.MusicService.Music;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {
    @Query("SELECT s FROM Song s WHERE s.userId = :userId")
    List<Song> getAllSongsByUserId(@Param("userId") UUID userId);
}

