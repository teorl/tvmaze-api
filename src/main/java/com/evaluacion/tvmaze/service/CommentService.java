package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.document.ShowComment;
import com.evaluacion.tvmaze.dto.CreateCommentRequest;
import com.evaluacion.tvmaze.repository.ShowCommentRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class CommentService {

    private final ShowService showService;
    private final ShowCommentRepository showCommentRepository;
    private final Clock clock;

    public CommentService(ShowService showService, ShowCommentRepository showCommentRepository, Clock clock) {
        this.showService = showService;
        this.showCommentRepository = showCommentRepository;
        this.clock = clock;
    }

    /**
     * Guarda el comentario de un show y devuelve su ID.
     *
     * @throws com.evaluacion.tvmaze.exception.ShowNotFoundException si el show no existe en TV Maze
     */
    public String addComment(long showId, CreateCommentRequest request) {
        // Se reutiliza el servicio con caché: valida que el show exista y evita llamar a TV Maze si ya está guardado.
        showService.getShow(showId);

        ShowComment comment = new ShowComment(null, showId, request.comment().strip(), request.rating(),
                Instant.now(clock));
        return showCommentRepository.save(comment).id();
    }
}
