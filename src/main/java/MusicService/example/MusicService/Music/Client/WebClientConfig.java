package MusicService.example.MusicService.Music.Client;


import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
=======
import org.springframework.web.reactive.function.client.WebClient;
>>>>>>> fd3f615d09ab37de336f579bd954a250c89f16c5

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
<<<<<<< HEAD
        return WebClient.builder()
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        clientRequest -> {
                            System.out.println("Request to: " + clientRequest.url());
                            return Mono.just(clientRequest);
                        }
                ));
    }
}
=======
        return WebClient.builder();
    }
}
>>>>>>> fd3f615d09ab37de336f579bd954a250c89f16c5
