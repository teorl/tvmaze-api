package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.client.dto.TvMazeSearchResult;
import com.evaluacion.tvmaze.document.CachedShow;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.mapper.ShowMapper;
import com.evaluacion.tvmaze.repository.CachedShowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowService.class);

    private final TvMazeClient tvMazeClient;
    private final ShowMapper showMapper;
    private final CachedShowRepository cachedShowRepository;
    private final Clock clock;

    public ShowService(TvMazeClient tvMazeClient, ShowMapper showMapper,
                       CachedShowRepository cachedShowRepository, Clock clock) {
        this.tvMazeClient = tvMazeClient;
        this.showMapper = showMapper;
        this.cachedShowRepository = cachedShowRepository;
        this.clock = clock;
    }

    public List<ShowSummaryResponse> searchShows(String query) {
        return tvMazeClient.searchShows(query.trim()).stream()
                .map(TvMazeSearchResult::show)
                .filter(Objects::nonNull)
                .map(showMapper::toSummary)
                .toList();
    }

    /**
     * Devuelve el show desde el caché en Mongo; si no está, lo consulta en TV Maze y lo guarda.
     */
    public Map<String, Object> getShow(long showId) {
        Optional<CachedShow> cached = cachedShowRepository.findById(showId);
        if (cached.isPresent()) {
            log.debug("Cache hit del show {}", showId);
            return cached.get().data();
        }
        log.info("Cache miss del show {}, consultando TV Maze", showId);
        return fetchAndCache(showId);
    }

    private Map<String, Object> fetchAndCache(long showId) {
        Map<String, Object> show = tvMazeClient.getShow(showId);
        cachedShowRepository.save(new CachedShow(showId, show, Instant.now(clock)));
        return show;
    }
}
