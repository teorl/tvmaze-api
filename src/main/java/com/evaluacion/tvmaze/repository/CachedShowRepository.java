package com.evaluacion.tvmaze.repository;

import com.evaluacion.tvmaze.document.CachedShow;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CachedShowRepository extends MongoRepository<CachedShow, Long> {
}
