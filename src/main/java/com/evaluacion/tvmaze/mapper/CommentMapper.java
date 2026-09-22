package com.evaluacion.tvmaze.mapper;

import com.evaluacion.tvmaze.document.ShowComment;
import com.evaluacion.tvmaze.dto.CommentResponse;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(ShowComment comment) {
        return new CommentResponse(comment.comment(), comment.rating());
    }
}
