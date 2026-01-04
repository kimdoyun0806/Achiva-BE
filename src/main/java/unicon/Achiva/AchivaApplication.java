package unicon.Achiva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AchivaApplication {
//
    public static void main(String[] args) {
        SpringApplication.run(AchivaApplication.class, args);
    }

}
