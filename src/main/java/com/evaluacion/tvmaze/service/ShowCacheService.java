package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.document.CachedShow;
import com.evaluacion.tvmaze.repository.CachedShowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Obtiene shows de TV Maze usando la colección {@code shows} de MongoDB como caché.
 */
@Service
public class ShowCacheService {

    private static final Logger log = LoggerFactory.getLogger(ShowCacheService.class);

    private final TvMazeClient tvMazeClient;
    private final CachedShowRepository cachedShowRepository;
    private final Clock clock;

    public ShowCacheService(TvMazeClient tvMazeClient, CachedShowRepository cachedShowRepository, Clock clock) {
        this.tvMazeClient = tvMazeClient;
        this.cachedShowRepository = cachedShowRepository;
        this.clock = clock;
    }

    /**
     * Devuelve el show desde el caché en Mongo; si no está, lo consulta en TV Maze y lo guarda.
     *
     * @throws com.evaluacion.tvmaze.exception.ShowNotFoundException si el show no existe en TV Maze
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
