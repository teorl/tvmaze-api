package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.document.ShowComment;
import com.evaluacion.tvmaze.dto.CommentResponse;
import com.evaluacion.tvmaze.dto.CreateCommentRequest;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.mapper.CommentMapper;
import com.evaluacion.tvmaze.repository.ShowCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");

    @Mock
    private ShowCacheService showCacheService;

    @Mock
    private ShowCommentRepository showCommentRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(showCacheService, showCommentRepository, new CommentMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesCommentWhenShowExists() {
        when(showCacheService.getShow(139)).thenReturn(Map.of("id", 139));
        ShowComment expectedToSave = new ShowComment(null, 139L, "Muy buena serie", 4, NOW);
        when(showCommentRepository.save(expectedToSave))
                .thenReturn(new ShowComment("abc123", 139L, "Muy buena serie", 4, NOW));

        String id = commentService.addComment(139, new CreateCommentRequest("  Muy buena serie ", 4));

        assertThat(id).isEqualTo("abc123");
        verify(showCommentRepository).save(expectedToSave);
    }

    @Test
    void doesNotSaveCommentWhenShowDoesNotExist() {
        when(showCacheService.getShow(999)).thenThrow(new ShowNotFoundException(999));

        assertThatThrownBy(() -> commentService.addComment(999, new CreateCommentRequest("Texto", 3)))
                .isInstanceOf(ShowNotFoundException.class);
        verify(showCommentRepository, never()).save(any());
    }

    @Test
    void groupsCommentsByShowIdKeepingChronologicalOrder() {
        Set<Long> showIds = Set.of(1L, 2L, 3L);
        when(showCommentRepository.findByShowIdIn(showIds, Sort.by("createdAt"))).thenReturn(List.of(
                new ShowComment("a", 1L, "Primero", 5, NOW.minusSeconds(60)),
                new ShowComment("b", 2L, "Otro show", 3, NOW.minusSeconds(30)),
                new ShowComment("c", 1L, "Segundo", 2, NOW)));

        Map<Long, List<CommentResponse>> result = commentService.findCommentsByShowIds(showIds);

        assertThat(result).containsOnly(
                Map.entry(1L, List.of(new CommentResponse("Primero", 5), new CommentResponse("Segundo", 2))),
                Map.entry(2L, List.of(new CommentResponse("Otro show", 3))));
    }

    @Test
    void doesNotQueryMongoWhenThereAreNoShowIds() {
        assertThat(commentService.findCommentsByShowIds(Set.of())).isEmpty();
        verifyNoInteractions(showCommentRepository);
    }
}
