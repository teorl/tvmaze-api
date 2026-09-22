package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.client.dto.TvMazeChannel;
import com.evaluacion.tvmaze.client.dto.TvMazeSearchResult;
import com.evaluacion.tvmaze.client.dto.TvMazeShow;
import com.evaluacion.tvmaze.dto.CommentResponse;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.mapper.ShowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private TvMazeClient tvMazeClient;

    @Mock
    private ShowCacheService showCacheService;

    @Mock
    private CommentService commentService;

    private ShowService showService;

    @BeforeEach
    void setUp() {
        showService = new ShowService(tvMazeClient, showCacheService, commentService, new ShowMapper());
    }

    @Test
    void searchAddsCommentsToEachShowUsingASingleQuery() {
        TvMazeShow girls = new TvMazeShow(139L, "Girls", "<p>Resumen</p>", List.of("Drama", "Romance"),
                new TvMazeChannel(8L, "HBO"), null);
        TvMazeShow girlsFive = new TvMazeShow(41734L, "Girls5eva", null, List.of("Comedy"),
                null, new TvMazeChannel(3L, "Netflix"));
        when(tvMazeClient.searchShows("girls")).thenReturn(List.of(
                new TvMazeSearchResult(0.9, girls),
                new TvMazeSearchResult(0.8, girlsFive)));
        List<CommentResponse> girlsComments = List.of(new CommentResponse("Muy buena", 4),
                new CommentResponse("Regular", 2));
        when(commentService.findCommentsByShowIds(Set.of(139L, 41734L)))
                .thenReturn(Map.of(139L, girlsComments));

        List<ShowSummaryResponse> results = showService.searchShows("  girls ");

        verify(tvMazeClient).searchShows("girls");
        verify(commentService, times(1)).findCommentsByShowIds(any());
        assertThat(results).containsExactly(
                new ShowSummaryResponse(139L, "Girls", "HBO", "<p>Resumen</p>", List.of("Drama", "Romance"),
                        girlsComments),
                new ShowSummaryResponse(41734L, "Girls5eva", "Netflix", null, List.of("Comedy"), List.of()));
    }

    @Test
    void searchReturnsEmptyListWhenThereAreNoResults() {
        when(tvMazeClient.searchShows("xyz")).thenReturn(List.of());
        when(commentService.findCommentsByShowIds(Set.of())).thenReturn(Map.of());

        assertThat(showService.searchShows("xyz")).isEmpty();
    }

    @Test
    void searchReturnsEmptyCommentsForShowWithoutId() {
        TvMazeShow withoutId = new TvMazeShow(null, "Sin ID", null, null, null, null);
        when(tvMazeClient.searchShows("sin id")).thenReturn(List.of(new TvMazeSearchResult(0.5, withoutId)));
        when(commentService.findCommentsByShowIds(Set.of())).thenReturn(Map.of());

        List<ShowSummaryResponse> results = showService.searchShows("sin id");

        assertThat(results).singleElement()
                .extracting(ShowSummaryResponse::comments)
                .isEqualTo(List.of());
    }

    @Test
    void getShowAddsCommentsToTheShow() {
        Map<String, Object> cachedShow = Map.of("id", 139, "name", "Girls");
        List<CommentResponse> comments = List.of(new CommentResponse("Muy buena", 4));
        when(showCacheService.getShow(139)).thenReturn(cachedShow);
        when(commentService.findCommentsByShowId(139)).thenReturn(comments);

        Map<String, Object> result = showService.getShow(139);

        assertThat(result)
                .containsEntry("id", 139)
                .containsEntry("name", "Girls")
                .containsEntry("comments", comments);
    }

    @Test
    void getShowReturnsEmptyCommentsWhenShowHasNone() {
        when(showCacheService.getShow(139)).thenReturn(Map.of("id", 139));
        when(commentService.findCommentsByShowId(139)).thenReturn(List.of());

        assertThat(showService.getShow(139)).containsEntry("comments", List.of());
    }

    @Test
    void getShowDoesNotModifyTheCachedShow() {
        Map<String, Object> cachedShow = new HashMap<>(Map.of("id", 139, "name", "Girls"));
        when(showCacheService.getShow(139)).thenReturn(cachedShow);
        when(commentService.findCommentsByShowId(139)).thenReturn(List.of(new CommentResponse("Muy buena", 4)));

        showService.getShow(139);

        assertThat(cachedShow).doesNotContainKey("comments");
    }

    @Test
    void getShowDoesNotLoadCommentsWhenShowDoesNotExist() {
        when(showCacheService.getShow(999)).thenThrow(new ShowNotFoundException(999));

        assertThatThrownBy(() -> showService.getShow(999))
                .isInstanceOf(ShowNotFoundException.class);
        verifyNoInteractions(commentService);
    }
}
