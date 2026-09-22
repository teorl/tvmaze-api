package com.evaluacion.tvmaze.dto;

/**
 * Comentario de un show tal como se incluye en las respuestas de la API.
 */
public record CommentResponse(String comment, int rating) {
}
