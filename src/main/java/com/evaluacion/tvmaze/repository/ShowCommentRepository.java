package com.evaluacion.tvmaze.repository;

import com.evaluacion.tvmaze.document.ShowComment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShowCommentRepository extends MongoRepository<ShowComment, String> {
}
