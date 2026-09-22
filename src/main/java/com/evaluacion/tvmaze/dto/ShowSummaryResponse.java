package com.evaluacion.tvmaze.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Respuesta del endpoint de búsqueda: versión reducida del show.
 *
 * @param channel  nombre de la cadena ({@code network.name}) o, si no existe,
 *                 del canal web ({@code webChannel.name}).
 * @param comments comentarios guardados para el show; arreglo vacío si no tiene.
 */
@Schema(description = "Versión reducida de un show, con sus comentarios")
public record ShowSummaryResponse(
        @Schema(description = "ID del show en TV Maze", example = "139")
        Long id,

        @Schema(description = "Nombre del show", example = "Girls")
        String name,

        @Schema(description = "Cadena de TV o, si no tiene, canal web", example = "HBO", nullable = true)
        String channel,

        @Schema(description = "Resumen en HTML", example = "<p>This Emmy winning series is a comic look at...</p>",
                nullable = true)
        String summary,

        @ArraySchema(arraySchema = @Schema(description = "Géneros del show"),
                schema = @Schema(example = "Drama"))
        List<String> genres,

        @ArraySchema(arraySchema = @Schema(description = "Comentarios guardados del show; vacío si no tiene"))
        List<CommentResponse> comments
) {
}
