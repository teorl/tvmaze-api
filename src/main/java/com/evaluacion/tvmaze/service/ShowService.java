package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.client.dto.TvMazeSearchResult;
import com.evaluacion.tvmaze.client.dto.TvMazeShow;
import com.evaluacion.tvmaze.dto.CommentResponse;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.mapper.ShowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShowService {

    private final TvMazeClient tvMazeClient;
    private final ShowCacheService showCacheService;
    private final CommentService commentService;
    private final ShowMapper showMapper;

    public ShowService(TvMazeClient tvMazeClient, ShowCacheService showCacheService,
                       CommentService commentService, ShowMapper showMapper) {
        this.tvMazeClient = tvMazeClient;
        this.showCacheService = showCacheService;
        this.commentService = commentService;
        this.showMapper = showMapper;
    }

    /**
     * Busca shows en TV Maze y agrega a cada uno sus comentarios, obtenidos con una sola consulta a Mongo.
     */
    public List<ShowSummaryResponse> searchShows(String query) {
        List<TvMazeShow> shows = tvMazeClient.searchShows(query.trim()).stream()
                .map(TvMazeSearchResult::show)
                .filter(Objects::nonNull)
                .toList();

        Set<Long> showIds = shows.stream()
                .map(TvMazeShow::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, List<CommentResponse>> commentsByShowId = commentService.findCommentsByShowIds(showIds);

        return shows.stream()
                .map(show -> showMapper.toSummary(show, commentsOf(show, commentsByShowId)))
                .toList();
    }

    public Map<String, Object> getShow(long showId) {
        return showCacheService.getShow(showId);
    }

    private static List<CommentResponse> commentsOf(TvMazeShow show,
                                                    Map<Long, List<CommentResponse>> commentsByShowId) {
        if (show.id() == null) {
            return List.of();
        }
        return commentsByShowId.getOrDefault(show.id(), List.of());
    }
}
