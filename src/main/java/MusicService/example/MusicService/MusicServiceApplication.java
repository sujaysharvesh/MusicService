package MusicService.example.MusicService;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
<<<<<<< HEAD
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

=======
import org.springframework.context.annotation.ComponentScan;
>>>>>>> fd3f615d09ab37de336f579bd954a250c89f16c5

import java.util.Collections;

@SpringBootApplication
@ComponentScan(basePackages = "MusicService.example.MusicService")
@EntityScan(basePackages = "MusicService.example.MusicService.Music")
public class MusicServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(MusicServiceApplication.class);
	public static void main(String[] args) {
		int port = 8082;
		SpringApplication app = new SpringApplication(MusicServiceApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", 8082));
		app.run(args);
		logger.info("Application running successfully on port {}", port);
	}

}
