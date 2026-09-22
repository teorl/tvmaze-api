package com.evaluacion.tvmaze.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configura el {@link RestClient} que usa {@code TvMazeClient} para llamar a TV Maze.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient tvMazeRestClient(RestClient.Builder builder, TvMazeProperties properties) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
