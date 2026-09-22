package com.evaluacion.tvmaze.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/shows/{showId}/comments}.
 */
public record CreateCommentRequest(
        @NotBlank(message = "es obligatorio y no puede estar vacío")
        @Size(max = 500, message = "no puede tener más de 500 caracteres")
        String comment,

        @NotNull(message = "es obligatorio")
        @Min(value = 0, message = "debe ser mayor o igual a 0")
        @Max(value = 5, message = "debe ser menor o igual a 5")
        Integer rating
) {
}
