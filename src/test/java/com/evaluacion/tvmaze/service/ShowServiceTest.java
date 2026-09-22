package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.client.dto.TvMazeChannel;
import com.evaluacion.tvmaze.client.dto.TvMazeSearchResult;
import com.evaluacion.tvmaze.client.dto.TvMazeShow;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.mapper.ShowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private TvMazeClient tvMazeClient;

    private ShowService showService;

    @BeforeEach
    void setUp() {
        showService = new ShowService(tvMazeClient, new ShowMapper());
    }

    @Test
    void mapsSearchResultsToSummaries() {
        TvMazeShow show = new TvMazeShow(139L, "Girls", "<p>Resumen</p>", List.of("Drama", "Romance"),
                new TvMazeChannel(8L, "HBO"), null);
        when(tvMazeClient.searchShows("girls")).thenReturn(List.of(new TvMazeSearchResult(0.9, show)));

        List<ShowSummaryResponse> results = showService.searchShows("  girls ");

        verify(tvMazeClient).searchShows("girls");
        assertThat(results).containsExactly(
                new ShowSummaryResponse(139L, "Girls", "HBO", "<p>Resumen</p>", List.of("Drama", "Romance")));
    }

    @Test
    void returnsEmptyListWhenThereAreNoResults() {
        when(tvMazeClient.searchShows("xyz")).thenReturn(List.of());

        assertThat(showService.searchShows("xyz")).isEmpty();
    }
}
