package MusicService.example.MusicService.Music;

import MusicService.example.MusicService.Music.Client.UserServiceClient;
import MusicService.example.MusicService.Music.DTO.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/music")
public class SongController {

    private static Logger logger = LoggerFactory.getLogger(SongController.class);

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
    public Mono<ResponseEntity<List<SongResponse>>> getUserSongs(@RequestHeader("Authorization") String token) {
        return userServiceClient.getUserInfo(token).flatMap(userDetailsDTO -> {
            List<SongResponse> songs = songService.getUserSongs(userDetailsDTO.getId()).stream()
                    .map(song -> {
                        return new SongResponse(song);
                    })
                    .collect(Collectors.toList());
            return Mono.just(ResponseEntity.ok(songs));
        }).onErrorResume(ex -> {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>()));
        });
    }

    public Mono<ResponseEntity<List<SongResponse>>> fallbackGetUserSongs(
            @RequestHeader("Authorization") String token, Throwable t) {
        logger.error("Circuit breaker triggered for getUserSongs: {}", t.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(List.of()));
    }

    @GetMapping(value = "/allsongs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<?>>> allSongs() {
        List<Song> allSongs = songService.getAllSongs();
        return Mono.just(ResponseEntity.status(HttpStatus.OK).body(allSongs));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<SongUploadResponseDTO>>> uploadMusic(
            @RequestHeader("Authorization") String token,
            @RequestPart("files") MultipartFile[] files) {

        if (files == null || files.length == 0) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(List.of(new SongUploadResponseDTO("No files provided"))));
        }


        return userServiceClient.getUserInfo(token)
                .timeout(Duration.ofSeconds(10))
                .flatMap(user -> {
                    if (user.getId() == null) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(List.of(new SongUploadResponseDTO("User ID is missing"))));
                    }

                    return Flux.fromArray(files)
                            .flatMap(file -> processSingleFile(file, user.getId()))
                            .collectList()
                            .map(responses -> {
                                boolean allSuccess = responses.stream()
                                        .allMatch(response -> response.getErrMessage() == null);

                                HttpStatus status = allSuccess ? HttpStatus.OK :
                                        responses.stream().anyMatch(r -> r.getErrMessage()!= null &&
                                                r.getErrMessage().contains("Unsupported"))
                                                ? HttpStatus.UNSUPPORTED_MEDIA_TYPE
                                                : HttpStatus.MULTI_STATUS;

                                return ResponseEntity.status(status).body(responses);
                            });
                })
                .onErrorResume(this::handleErrorCases);
    }

    private Mono<SongUploadResponseDTO> processSingleFile(MultipartFile file, UUID userId) {
        logger.info("file info: " + file.getOriginalFilename() + " " + userId);
        if (file.isEmpty()) {
            return Mono.just(new SongUploadResponseDTO("Empty file: " + file.getOriginalFilename()));
        }

        String rawContentType = file.getContentType();
        logger.info("file content type: " + rawContentType);
        if (rawContentType == null) {
            return Mono.just(new SongUploadResponseDTO(
                    "Content type cannot be determined for: " + file.getOriginalFilename()));
        }

        MediaType contentType = MediaType.parseMediaType(rawContentType);
        if (!isAudioContentType(contentType)) {
            return Mono.just(new SongUploadResponseDTO(
                    "Unsupported file type: " + contentType + " for: " + file.getOriginalFilename()));
        }

        try {
            Song song = songService.uploadSong(file, userId);
            logger.info("Song successfully uploaded: id={}, s3Key={}", song.getId(), song.getS3Key());

            SongUploadResponseDTO responseDTO = new SongUploadResponseDTO(song, userId);

            logger.info("Created response DTO: {}", responseDTO);
            return Mono.just(responseDTO);
        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return Mono.just(new SongUploadResponseDTO(
                    "Error processing " + file.getOriginalFilename() + ": " + e.getMessage()));
        }
    }

    private Mono<ResponseEntity<List<SongUploadResponseDTO>>> handleErrorCases(Throwable ex) {
        if (ex instanceof WebClientResponseException.Unauthorized) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(List.of(new SongUploadResponseDTO("Unauthorized: Invalid token"))));
        } else if (ex instanceof TimeoutException) {
            return Mono.just(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body(List.of(new SongUploadResponseDTO("Request timed out"))));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(new SongUploadResponseDTO("Unexpected error: " + ex.getMessage()))));
        }
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