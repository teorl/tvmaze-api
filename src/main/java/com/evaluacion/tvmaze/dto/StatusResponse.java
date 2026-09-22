package com.evaluacion.tvmaze.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Respuesta genérica para operaciones de escritura.
 *
 * @param id identificador del recurso creado.
 */
@Schema(description = "Resultado de una operación de escritura")
public record StatusResponse(
        @Schema(description = "Resultado de la operación", example = "success")
        String status,

        @Schema(description = "Mensaje descriptivo", example = "Comentario guardado")
        String message,

        @Schema(description = "ID del recurso creado", example = "66f0c2a1e4b0a1b2c3d4e5f6")
        String id
) {

    public static StatusResponse success(String message, String id) {
        return new StatusResponse("success", message, id);
    }
}
