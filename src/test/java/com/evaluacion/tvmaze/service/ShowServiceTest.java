package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.client.dto.TvMazeChannel;
import com.evaluacion.tvmaze.client.dto.TvMazeSearchResult;
import com.evaluacion.tvmaze.client.dto.TvMazeShow;
import com.evaluacion.tvmaze.document.CachedShow;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.mapper.ShowMapper;
import com.evaluacion.tvmaze.repository.CachedShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");

    @Mock
    private TvMazeClient tvMazeClient;

    @Mock
    private CachedShowRepository cachedShowRepository;

    private ShowService showService;

    @BeforeEach
    void setUp() {
        showService = new ShowService(tvMazeClient, new ShowMapper(), cachedShowRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
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

    @Test
    void getShowReturnsCachedShowWithoutCallingTvMaze() {
        Map<String, Object> cached = Map.of("id", 139, "name", "Girls");
        when(cachedShowRepository.findById(139L))
                .thenReturn(Optional.of(new CachedShow(139L, cached, NOW.minusSeconds(3600))));

        Map<String, Object> result = showService.getShow(139);

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(tvMazeClient);
        verify(cachedShowRepository, never()).save(any());
    }

    @Test
    void getShowFetchesFromTvMazeAndCachesItWhenNotCached() {
        Map<String, Object> show = Map.of("id", 139, "name", "Girls");
        when(cachedShowRepository.findById(139L)).thenReturn(Optional.empty());
        when(tvMazeClient.getShow(139)).thenReturn(show);

        Map<String, Object> result = showService.getShow(139);

        assertThat(result).isEqualTo(show);
        verify(cachedShowRepository).save(new CachedShow(139L, show, NOW));
    }

    @Test
    void getShowDoesNotCacheWhenShowDoesNotExist() {
        when(cachedShowRepository.findById(999L)).thenReturn(Optional.empty());
        when(tvMazeClient.getShow(999)).thenThrow(new ShowNotFoundException(999));

        assertThatThrownBy(() -> showService.getShow(999))
                .isInstanceOf(ShowNotFoundException.class);
        verify(cachedShowRepository, never()).save(any());
    }
}
