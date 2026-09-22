package com.evaluacion.tvmaze.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Calificación y comentario de un usuario sobre un show (colección {@code comments}).
 *
 * @param id        ID generado por MongoDB; es {@code null} antes de guardar.
 * @param showId    ID del show en TV Maze. Indexado porque todas las consultas filtran por este campo.
 * @param comment   texto del comentario.
 * @param rating    calificación de 0 a 5.
 * @param createdAt momento en que se registró el comentario.
 */
@Document("comments")
public record ShowComment(
        @Id String id,
        @Indexed Long showId,
        String comment,
        int rating,
        Instant createdAt
) {
}
