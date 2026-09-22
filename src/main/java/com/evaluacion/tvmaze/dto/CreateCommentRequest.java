package com.evaluacion.tvmaze.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/shows/{showId}/comments}.
 */
@Schema(description = "Comentario y calificación de un show")
public record CreateCommentRequest(
        @Schema(description = "Texto del comentario", example = "Muy buena serie", maxLength = 500)
        @NotBlank(message = "es obligatorio y no puede estar vacío")
        @Size(max = 500, message = "no puede tener más de 500 caracteres")
        String comment,

        @Schema(description = "Calificación de 0 a 5", example = "4", minimum = "0", maximum = "5")
        @NotNull(message = "es obligatorio")
        @Min(value = 0, message = "debe ser mayor o igual a 0")
        @Max(value = 5, message = "debe ser menor o igual a 5")
        Integer rating
) {
}
