package com.evaluacion.tvmaze.mapper;

import com.evaluacion.tvmaze.document.ShowComment;
import com.evaluacion.tvmaze.dto.CommentResponse;
import org.springframework.stereotype.Component;

/**
 * Convierte los comentarios guardados en Mongo a los DTOs de respuesta de la API.
 */
@Component
public class CommentMapper {

    public CommentResponse toResponse(ShowComment comment) {
        return new CommentResponse(comment.comment(), comment.rating());
    }
}
