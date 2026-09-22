package com.evaluacion.tvmaze.client;

import com.evaluacion.tvmaze.client.dto.TvMazeSearchResult;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.exception.TvMazeUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsula toda la comunicación HTTP con el API público de TV Maze.
 */
@Component
public class TvMazeClient {

    private static final ParameterizedTypeReference<List<TvMazeSearchResult>> SEARCH_RESULT_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Map<String, Object>> SHOW_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public TvMazeClient(RestClient tvMazeRestClient) {
        this.restClient = tvMazeRestClient;
    }

    public List<TvMazeSearchResult> searchShows(String query) {
        try {
            List<TvMazeSearchResult> results = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/shows")
                            .queryParam("q", query)
                            .build())
                    .retrieve()
                    .body(SEARCH_RESULT_TYPE);
            return Objects.requireNonNullElse(results, List.of());
        } catch (RestClientException ex) {
            throw new TvMazeUnavailableException("Error al consultar la búsqueda de shows en TV Maze", ex);
        }
    }

    /**
     * Obtiene el show completo tal como lo devuelve TV Maze, sin descartar ningún campo.
     *
     * @throws ShowNotFoundException si TV Maze responde 404
     */
    public Map<String, Object> getShow(long showId) {
        try {
            Map<String, Object> show = restClient.get()
                    .uri("/shows/{id}", showId)
                    .retrieve()
                    .body(SHOW_TYPE);
            if (show == null) {
                throw new ShowNotFoundException(showId);
            }
            return show;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ShowNotFoundException(showId);
        } catch (RestClientException ex) {
            throw new TvMazeUnavailableException("Error al consultar el show %d en TV Maze".formatted(showId), ex);
        }
    }
}
