package MusicService.example.MusicService.Music.Client;

import MusicService.example.MusicService.Music.DTO.UserDetailsDTO;
<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
=======
import org.springframework.beans.factory.annotation.Value;
>>>>>>> fd3f615d09ab37de336f579bd954a250c89f16c5
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

<<<<<<< HEAD
@Slf4j
@Component
public class UserServiceClient {
    private final WebClient webClient;

    public UserServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://user-service") // Must match Eureka registration name
=======

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder, @Value("${user-service.url}") String userServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(userServiceUrl)
>>>>>>> fd3f615d09ab37de336f579bd954a250c89f16c5
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

<<<<<<< HEAD
    public Mono<UserDetailsDTO> getUserInfo(String token) {
=======
    public Mono<UserDetailsDTO> getUserInfo(String token){
>>>>>>> fd3f615d09ab37de336f579bd954a250c89f16c5
        return webClient.get()
                .uri("/api/user/me")
                .header("Authorization", token)
                .retrieve()
<<<<<<< HEAD
                .onStatus(status -> status.isError(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException(
                                    "User service error: " + response.statusCode() + " - " + body)));
                })
                .bodyToMono(UserDetailsDTO.class);
    }
=======
                .bodyToMono(UserDetailsDTO.class);
    }

>>>>>>> fd3f615d09ab37de336f579bd954a250c89f16c5
}
