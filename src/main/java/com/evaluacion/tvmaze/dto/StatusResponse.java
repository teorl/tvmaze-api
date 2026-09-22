package com.evaluacion.tvmaze.dto;

/**
 * Respuesta genérica para operaciones de escritura.
 *
 * @param id identificador del recurso creado.
 */
public record StatusResponse(String status, String message, String id) {

    public static StatusResponse success(String message, String id) {
        return new StatusResponse("success", message, id);
    }
}
