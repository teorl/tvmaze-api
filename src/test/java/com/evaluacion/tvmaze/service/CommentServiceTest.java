package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.document.ShowComment;
import com.evaluacion.tvmaze.dto.CreateCommentRequest;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.repository.ShowCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");

    @Mock
    private ShowService showService;

    @Mock
    private ShowCommentRepository showCommentRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(showService, showCommentRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesCommentWhenShowExists() {
        when(showService.getShow(139)).thenReturn(Map.of("id", 139));
        ShowComment expectedToSave = new ShowComment(null, 139L, "Muy buena serie", 4, NOW);
        when(showCommentRepository.save(expectedToSave))
                .thenReturn(new ShowComment("abc123", 139L, "Muy buena serie", 4, NOW));

        String id = commentService.addComment(139, new CreateCommentRequest("  Muy buena serie ", 4));

        assertThat(id).isEqualTo("abc123");
        verify(showCommentRepository).save(expectedToSave);
    }

    @Test
    void doesNotSaveCommentWhenShowDoesNotExist() {
        when(showService.getShow(999)).thenThrow(new ShowNotFoundException(999));

        assertThatThrownBy(() -> commentService.addComment(999, new CreateCommentRequest("Texto", 3)))
                .isInstanceOf(ShowNotFoundException.class);
        verify(showCommentRepository, never()).save(any());
    }
}
