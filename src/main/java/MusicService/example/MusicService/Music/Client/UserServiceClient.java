package MusicService.example.MusicService.Music.Client;

import MusicService.example.MusicService.Music.DTO.UserDetailsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder, @Value("${user-service.url}") String userServiceUrl) {
        this.webClient = webClientBuilder
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
