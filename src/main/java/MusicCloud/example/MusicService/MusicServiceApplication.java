package MusicCloud.example.MusicService;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;

import java.util.Collections;

@SpringBootApplication
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
