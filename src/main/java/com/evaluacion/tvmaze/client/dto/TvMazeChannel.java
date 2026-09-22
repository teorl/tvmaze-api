package com.evaluacion.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa tanto {@code network} como {@code webChannel} en la respuesta de TV Maze.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeChannel(Long id, String name) {
}
