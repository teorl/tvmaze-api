package com.evaluacion.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cada elemento del arreglo que devuelve {@code GET /search/shows}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeSearchResult(Double score, TvMazeShow show) {
}
