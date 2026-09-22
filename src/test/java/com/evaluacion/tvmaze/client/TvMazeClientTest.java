package com.evaluacion.tvmaze.client;

import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.exception.TvMazeUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TvMazeClientTest {

    private static final String BASE_URL = "https://api.tvmaze.com";

    private MockRestServiceServer server;
    private TvMazeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TvMazeClient(builder.build());
    }

    @Test
    void getShowReturnsAllFieldsFromTvMaze() {
        server.expect(requestTo(BASE_URL + "/shows/139"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": 139,
                          "name": "Girls",
                          "genres": ["Drama", "Romance"],
                          "rating": {"average": 6.5},
                          "_links": {"self": {"href": "https://api.tvmaze.com/shows/139"}}
                        }
                        """, MediaType.APPLICATION_JSON));

        Map<String, Object> show = client.getShow(139);

        server.verify();
        assertThat(show)
                .containsEntry("id", 139)
                .containsEntry("name", "Girls")
                .containsEntry("genres", List.of("Drama", "Romance"))
                .containsEntry("rating", Map.of("average", 6.5))
                .containsKey("_links");
    }

    @Test
    void getShowThrowsShowNotFoundWhenTvMazeReturns404() {
        server.expect(requestTo(BASE_URL + "/shows/999999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getShow(999999))
                .isInstanceOf(ShowNotFoundException.class)
                .hasMessage("No existe un show con ID 999999");
    }

    @Test
    void getShowThrowsTvMazeUnavailableOnServerError() {
        server.expect(requestTo(BASE_URL + "/shows/139"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getShow(139))
                .isInstanceOf(TvMazeUnavailableException.class);
    }
}
