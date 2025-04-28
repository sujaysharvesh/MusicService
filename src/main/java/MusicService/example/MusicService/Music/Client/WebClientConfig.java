package MusicService.example.MusicService.Music.Client;


import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        clientRequest -> {
                            System.out.println("Request to: " + clientRequest.url());
                            return Mono.just(clientRequest);
                        }
                ));
    }
}