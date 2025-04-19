package MusicCloud.example.MusicService.Music;


import MusicCloud.example.MusicService.Music.Client.UserServiceClient;
import MusicCloud.example.MusicService.Music.DTO.SongResponseDTO;
import MusicCloud.example.MusicService.Music.DTO.SongUploadResponseDTO;
import MusicCloud.example.MusicService.Music.DTO.UserDetailsDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.utils.StreamGobbler;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class SongController {

    @Autowired
    private S3Services s3Service;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private SongService songService;


    @GetMapping
    public String home() {
        return "Welcome to the music service";
    }
    private static final List<MediaType> SUPPORTED_AUDIO_TYPES = List.of(
            MediaType.valueOf("audio/mpeg"),  // MP3
            MediaType.valueOf("audio/mp4"),   // M4A/AAC
            MediaType.valueOf("audio/ogg"),   // OGG
            MediaType.valueOf("audio/wav")    // WAV
    );


    @GetMapping(value = "/me", produces = "application/json")
    public Mono<ResponseEntity<?>> getCurrentUser(@RequestHeader("Authorization") String token) {

        return userServiceClient.getUserInfo(token)
                .timeout(Duration.ofSeconds(5)) // Add timeout
                .map(user -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                        .body(user));
    }

    @GetMapping("/songs")
    @CircuitBreaker(name = "musicBreaker", fallbackMethod = "fallbackGetUserSongs")
    public Mono<ResponseEntity<List<SongResponseDTO>>> getUserSongs(@RequestHeader("Authorization") String token) {
        UserDetailsDTO userDetailsDTO = userServiceClient.getUserInfo(token).block();
        List<SongResponseDTO> songs = songService.getUserSongs(userDetailsDTO.getId()).stream()
                .map(song -> new SongResponseDTO(
                        song.getId(),
                        song.getTitle(),
                        song.getS3Key(),
                        song.getContentType(),
                        song.getDurationSec(),
                        song.getFileSize()
                )).toList();
        return Mono.just(ResponseEntity.ok(songs));
    }



    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SongUploadResponseDTO>> uploadMusic(
            @RequestHeader("Authorization") String token,
            @RequestPart("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SongUploadResponseDTO("No file provided")));
        }
        String rawContentType = file.getContentType();
        if (rawContentType == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SongUploadResponseDTO("File content type cannot be determined")));
        }
        MediaType contentType =MediaType.parseMediaType(file.getContentType());
        if (!isAudioContentType(contentType)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(new SongUploadResponseDTO("Unsupported file type: " + contentType)));
        }
        return userServiceClient.getUserInfo(token)
                .timeout(Duration.ofSeconds(5))
                .flatMap(user -> {
                    System.out.println("User ID: " + user.getId());
                    if (user.getId() == null) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new SongUploadResponseDTO("User ID is missing")));
                    }
                    try {
                        Song song = songService.uploadSong(file, user.getId());
                        return Mono.just(ResponseEntity.ok()
                                .body(new SongUploadResponseDTO(
                                        song.getId(),
                                        user.getId(),
                                        song.getS3Key(),
                                        song.getTitle(),
                                        song.getFileSize(),
                                        song.getDurationSec(),
                                        song.getContentType(),
                                        null,
                                        song.getUploadedAt()// No error message
                                )));
                    } catch (IOException | TikaException | SAXException | TagException | InvalidAudioFrameException |
                             ReadOnlyFileException | CannotReadException e) {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new SongUploadResponseDTO("Error processing file: " + e.getMessage())));
                    }
                })
                .onErrorResume(ex -> {
                    if (ex instanceof WebClientResponseException.Unauthorized) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new SongUploadResponseDTO("Unauthorized: Invalid token")));
                    } else if (ex instanceof TimeoutException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                                .body(new SongUploadResponseDTO("Request timed out")));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new SongUploadResponseDTO("Unexpected error: " + ex.getMessage())));
                    }
                });

    }
    private boolean isAudioContentType(MediaType contentType) {
        return SUPPORTED_AUDIO_TYPES.stream()
                .anyMatch(supported -> supported.includes(contentType));
    }

    @GetMapping("/stream/{songId}")
    public ResponseEntity<StreamingResponseBody> streamSong(
                        @PathVariable UUID songId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "range", required = false) String rangeHeader) throws IOException {
        Song song = songService.getSongById(songId);
        if (song == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(outputStream -> outputStream.write("{\"error\": \"Song not found\"}".getBytes()));
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(outputStream -> outputStream.write("{\"error\": \"Unauthorized\"}".getBytes()));
        }

        UserDetailsDTO userDetails = userServiceClient.getUserInfo(token).block();
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(outputStream -> outputStream.write("{\"error\": \"Invalid or expired token\"}".getBytes()));
        }

        UUID userId = userDetails.getId();

        if (!hasAccessToSong(userId, song)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(OutputStream -> OutputStream.write("{\"error\": \"Access denied\"}".getBytes()));
        }

        InputStream audioStream = s3Service.getFileStream(song.getS3Key());
        long fileSize = s3Service.getFileSize(song.getS3Key());
        if (rangeHeader!= null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(ranges[0]);
            long end = (ranges.length > 1 && !ranges[1].isEmpty()) ? Long.parseLong(ranges[1]) : fileSize - 1;
            if (start >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }
            long contentLength = end - start + 1;
            audioStream.skip(start);

            StreamingResponseBody stream = outputStream -> copyStream(audioStream, outputStream);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentType(MediaType.parseMediaType(song.getContentType()))
                    .contentLength(contentLength)
                    .body(stream);
        }

        StreamingResponseBody stream = outputStream -> copyStream(audioStream, outputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(song.getContentType()))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentLength(fileSize)
                .body(stream);
    }
    private void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private boolean hasAccessToSong(UUID userId, Song song) {
        return song.getUserId().equals(userId);
    }

    @DeleteMapping("/delete/{songId}")
    public ResponseEntity<?> deleteSong(
            @PathVariable UUID songId,
            @RequestHeader("Authorization") String token) {
        Song song = songService.getSongById(songId);
        if (song == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SongUploadResponseDTO("Song not found"));
        }
        UUID userId = userServiceClient.getUserInfo(token).block().getId();
        if (!hasAccessToSong(userId, song)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SongUploadResponseDTO("Access denied"));
        }
        songService.deleteSong(song.getId());
        return ResponseEntity.ok(new SongUploadResponseDTO("Song deleted successfully"));
    }
}