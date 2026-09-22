package com.evaluacion.tvmaze.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Copia en caché de un show de TV Maze (colección {@code shows}).
 *
 * @param id       ID del show en TV Maze; se usa como {@code _id} del documento.
 * @param data     show completo tal como lo devolvió TV Maze. No incluye comentarios.
 * @param cachedAt momento en que se guardó; permite agregar expiración más adelante.
 */
@Document("shows")
public record CachedShow(@Id Long id, Map<String, Object> data, Instant cachedAt) {
}
