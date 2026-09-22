package com.evaluacion.tvmaze.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración externa del API de TV Maze (prefijo {@code tvmaze} en application.yml).
 */
@Validated
@ConfigurationProperties(prefix = "tvmaze")
public record TvMazeProperties(@NotBlank String baseUrl) {
}
