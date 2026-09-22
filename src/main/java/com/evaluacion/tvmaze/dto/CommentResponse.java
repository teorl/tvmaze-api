package com.evaluacion.tvmaze.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Comentario de un show tal como se incluye en las respuestas de la API.
 */
@Schema(description = "Comentario guardado de un show")
public record CommentResponse(
        @Schema(description = "Texto del comentario", example = "Muy buena serie")
        String comment,

        @Schema(description = "Calificación de 0 a 5", example = "4")
        int rating
) {
}
