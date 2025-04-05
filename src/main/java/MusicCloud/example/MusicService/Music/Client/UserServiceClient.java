package MusicCloud.example.MusicService.Music.Client;

import MusicCloud.example.MusicService.Music.DTO.UserDetailsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.http.HttpResponse;
import java.net.http.WebSocket;


@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Value("${user-service.url}") String userServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<UserDetailsDTO> getUserInfo(String token){
        return webClient.get()
                .uri("/api/user/me")
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(UserDetailsDTO.class);
    }

}
