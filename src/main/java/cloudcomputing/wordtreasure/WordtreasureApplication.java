package cloudcomputing.wordtreasure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WordtreasureApplication {

    public static void main(String[] args) {
        SpringApplication.run(WordtreasureApplication.class, args);
    }

}
