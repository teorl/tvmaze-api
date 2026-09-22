package com.evaluacion.tvmaze.controller;

import com.evaluacion.tvmaze.dto.CreateCommentRequest;
import com.evaluacion.tvmaze.dto.StatusResponse;
import com.evaluacion.tvmaze.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows/{showId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Guarda la calificación y el comentario de un show. Ejemplo: {@code POST /api/shows/139/comments}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatusResponse addComment(@PathVariable @Positive(message = "debe ser un número positivo") long showId,
                                     @Valid @RequestBody CreateCommentRequest request) {
        String commentId = commentService.addComment(showId, request);
        return StatusResponse.success("Comentario guardado", commentId);
    }
}
