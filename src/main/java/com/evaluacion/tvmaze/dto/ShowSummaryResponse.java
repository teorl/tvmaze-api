package com.evaluacion.tvmaze.dto;

import java.util.List;

/**
 * Respuesta del endpoint de búsqueda: versión reducida del show.
 *
 * @param channel nombre de la cadena ({@code network.name}) o, si no existe,
 *                del canal web ({@code webChannel.name}).
 * @param comments comentarios guardados para el show; arreglo vacío si no tiene.
 */
public record ShowSummaryResponse(
        Long id,
        String name,
        String channel,
        String summary,
        List<String> genres,
        List<CommentResponse> comments
) {
}
