package com.evaluacion.tvmaze.mapper;

import com.evaluacion.tvmaze.client.dto.TvMazeChannel;
import com.evaluacion.tvmaze.client.dto.TvMazeShow;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShowMapperTest {

    private final ShowMapper mapper = new ShowMapper();

    @Test
    void usesNetworkNameWhenPresent() {
        TvMazeShow show = new TvMazeShow(1L, "Girls", "<p>Resumen</p>", List.of("Drama"),
                new TvMazeChannel(8L, "HBO"), new TvMazeChannel(2L, "Web"));

        ShowSummaryResponse result = mapper.toSummary(show);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Girls");
        assertThat(result.channel()).isEqualTo("HBO");
        assertThat(result.summary()).isEqualTo("<p>Resumen</p>");
        assertThat(result.genres()).containsExactly("Drama");
    }

    @Test
    void fallsBackToWebChannelWhenNetworkIsMissing() {
        TvMazeShow show = new TvMazeShow(2L, "Show web", null, List.of(), null,
                new TvMazeChannel(1L, "Netflix"));

        assertThat(mapper.toSummary(show).channel()).isEqualTo("Netflix");
    }

    @Test
    void returnsNullChannelAndEmptyGenresWhenDataIsMissing() {
        TvMazeShow show = new TvMazeShow(3L, "Sin canal", null, null, null, null);

        ShowSummaryResponse result = mapper.toSummary(show);

        assertThat(result.channel()).isNull();
        assertThat(result.genres()).isEmpty();
    }
}
