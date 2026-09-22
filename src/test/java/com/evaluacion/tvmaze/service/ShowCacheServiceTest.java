package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.document.CachedShow;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.repository.CachedShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class ShowCacheServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");

    @Mock
    private TvMazeClient tvMazeClient;

    @Mock
    private CachedShowRepository cachedShowRepository;

    private ShowCacheService showCacheService;

    @BeforeEach
    void setUp() {
        showCacheService = new ShowCacheService(tvMazeClient, cachedShowRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void returnsCachedShowWithoutCallingTvMaze() {
        Map<String, Object> cached = Map.of("id", 139, "name", "Girls");
        when(cachedShowRepository.findById(139L))
                .thenReturn(Optional.of(new CachedShow(139L, cached, NOW.minusSeconds(3600))));

        Map<String, Object> result = showCacheService.getShow(139);

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(tvMazeClient);
        verify(cachedShowRepository, never()).save(any());
    }

    @Test
    void fetchesFromTvMazeAndCachesItWhenNotCached() {
        Map<String, Object> show = Map.of("id", 139, "name", "Girls");
        when(cachedShowRepository.findById(139L)).thenReturn(Optional.empty());
        when(tvMazeClient.getShow(139)).thenReturn(show);

        Map<String, Object> result = showCacheService.getShow(139);

        assertThat(result).isEqualTo(show);
        verify(cachedShowRepository).save(new CachedShow(139L, show, NOW));
    }

    @Test
    void doesNotCacheWhenShowDoesNotExist() {
        when(cachedShowRepository.findById(999L)).thenReturn(Optional.empty());
        when(tvMazeClient.getShow(999)).thenThrow(new ShowNotFoundException(999));

        assertThatThrownBy(() -> showCacheService.getShow(999))
                .isInstanceOf(ShowNotFoundException.class);
        verify(cachedShowRepository, never()).save(any());
    }
}
