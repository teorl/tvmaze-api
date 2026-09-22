package com.evaluacion.tvmaze.repository;

import com.evaluacion.tvmaze.document.CachedShow;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Acceso a la colección {@code shows}, que funciona como caché de TV Maze.
 */
public interface CachedShowRepository extends MongoRepository<CachedShow, Long> {
}
