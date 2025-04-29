package MusicService.example.MusicService.Music.Client;

import MusicService.example.MusicService.Music.DTO.UserDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserServiceClient {
    private final WebClient webClient;

    public UserServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://user-service") // Must match Eureka registration name
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<UserDetailsDTO> getUserInfo(String token) {
        return webClient.get()
                .uri("/api/user/me")
                .header("Authorization", token)
                .retrieve()
                .onStatus(status -> status.isError(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException(
                                    "User service error: " + response.statusCode() + " - " + body)));
                })
                .bodyToMono(UserDetailsDTO.class);
    }
}
