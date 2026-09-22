package com.evaluacion.tvmaze.service;

import com.evaluacion.tvmaze.client.TvMazeClient;
import com.evaluacion.tvmaze.client.dto.TvMazeSearchResult;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.mapper.ShowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ShowService {

    private final TvMazeClient tvMazeClient;
    private final ShowMapper showMapper;

    public ShowService(TvMazeClient tvMazeClient, ShowMapper showMapper) {
        this.tvMazeClient = tvMazeClient;
        this.showMapper = showMapper;
    }

    public List<ShowSummaryResponse> searchShows(String query) {
        return tvMazeClient.searchShows(query.trim()).stream()
                .map(TvMazeSearchResult::show)
                .filter(Objects::nonNull)
                .map(showMapper::toSummary)
                .toList();
    }
}
