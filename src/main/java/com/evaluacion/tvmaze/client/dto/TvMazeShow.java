package com.evaluacion.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Subconjunto de campos del show de TV Maze que necesita el endpoint de búsqueda.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeShow(
        Long id,
        String name,
        String summary,
        List<String> genres,
        TvMazeChannel network,
        TvMazeChannel webChannel
) {
}
