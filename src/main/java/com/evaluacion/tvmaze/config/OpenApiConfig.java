package com.evaluacion.tvmaze.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Información general de la documentación OpenAPI (disponible en {@code /swagger-ui.html}).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tvMazeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TV Maze API Middleware")
                .version("1.0.0")
                .description("Middleware de TV Maze: búsqueda de shows, detalle con caché en MongoDB "
                        + "y comentarios con calificación por show."));
    }
}
