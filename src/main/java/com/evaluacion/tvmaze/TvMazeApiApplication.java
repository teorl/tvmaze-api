package com.evaluacion.tvmaze;

import com.evaluacion.tvmaze.config.MongoUriValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TvMazeApiApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TvMazeApiApplication.class);
        application.addListeners(new MongoUriValidator());
        application.run(args);
    }
}
