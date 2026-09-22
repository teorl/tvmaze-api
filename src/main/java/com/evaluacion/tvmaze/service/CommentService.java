package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.document.ShowComment;
import com.evaluacion.tvmaze.dto.CommentResponse;
import com.evaluacion.tvmaze.dto.CreateCommentRequest;
import com.evaluacion.tvmaze.mapper.CommentMapper;
import com.evaluacion.tvmaze.repository.ShowCommentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registro y consulta de los comentarios de los shows.
 */
@Service
public class CommentService {

    /**
     * Los comentarios de cada show se devuelven del más antiguo al más reciente.
     */
    private static final Sort CHRONOLOGICAL = Sort.by("createdAt");

    private final ShowCacheService showCacheService;
    private final ShowCommentRepository showCommentRepository;
    private final CommentMapper commentMapper;
    private final Clock clock;

    public CommentService(ShowCacheService showCacheService, ShowCommentRepository showCommentRepository,
                          CommentMapper commentMapper, Clock clock) {
        this.showCacheService = showCacheService;
        this.showCommentRepository = showCommentRepository;
        this.commentMapper = commentMapper;
        this.clock = clock;
    }

    /**
     * Guarda el comentario de un show y devuelve su ID.
     *
     * @throws com.evaluacion.tvmaze.exception.ShowNotFoundException si el show no existe en TV Maze
     */
    public String addComment(long showId, CreateCommentRequest request) {
        // Valida que el show exista; si ya está en caché no se llama a TV Maze.
        showCacheService.getShow(showId);

        ShowComment comment = new ShowComment(null, showId, request.comment().strip(), request.rating(),
                Instant.now(clock));
        return showCommentRepository.save(comment).id();
    }

    /**
     * Obtiene los comentarios de un show; lista vacía si no tiene.
     */
    public List<CommentResponse> findCommentsByShowId(long showId) {
        return findCommentsByShowIds(Set.of(showId)).getOrDefault(showId, List.of());
    }

    /**
     * Obtiene los comentarios de varios shows con una sola consulta a Mongo, agrupados por {@code showId}.
     * Los shows sin comentarios no aparecen en el mapa.
     */
    public Map<Long, List<CommentResponse>> findCommentsByShowIds(Collection<Long> showIds) {
        if (showIds.isEmpty()) {
            return Map.of();
        }
        return showCommentRepository.findByShowIdIn(showIds, CHRONOLOGICAL).stream()
                .collect(Collectors.groupingBy(ShowComment::showId,
                        Collectors.mapping(commentMapper::toResponse, Collectors.toList())));
    }
}
