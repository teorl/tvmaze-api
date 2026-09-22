package com.evaluacion.tvmaze.repository;

import com.evaluacion.tvmaze.document.ShowComment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ShowCommentRepository extends MongoRepository<ShowComment, String> {

    /**
     * Trae en una sola consulta ({@code showId: {$in: [...]}}) los comentarios de varios shows.
     */
    List<ShowComment> findByShowIdIn(Collection<Long> showIds, Sort sort);
}
