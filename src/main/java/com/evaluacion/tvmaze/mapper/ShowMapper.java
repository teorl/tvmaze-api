package com.evaluacion.tvmaze.mapper;

import com.evaluacion.tvmaze.client.dto.TvMazeChannel;
import com.evaluacion.tvmaze.client.dto.TvMazeShow;
import com.evaluacion.tvmaze.dto.CommentResponse;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class ShowMapper {

    public ShowSummaryResponse toSummary(TvMazeShow show, List<CommentResponse> comments) {
        return new ShowSummaryResponse(
                show.id(),
                show.name(),
                resolveChannel(show),
                show.summary(),
                Objects.requireNonNullElse(show.genres(), List.of()),
                comments
        );
    }

    /**
     * Prioriza la cadena de TV; si el show solo se transmite por web, usa el canal web.
     */
    private String resolveChannel(TvMazeShow show) {
        return Optional.ofNullable(show.network())
                .or(() -> Optional.ofNullable(show.webChannel()))
                .map(TvMazeChannel::name)
                .orElse(null);
    }
}
