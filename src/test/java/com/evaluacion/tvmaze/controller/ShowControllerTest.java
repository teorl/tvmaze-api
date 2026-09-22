package com.evaluacion.tvmaze.controller;

import com.evaluacion.tvmaze.dto.CommentResponse;
import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.exception.TvMazeUnavailableException;
import com.evaluacion.tvmaze.service.ShowService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowController.class)
class ShowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShowService showService;

    @Nested
    class Search {

        @Test
        void returns200WithShowsAndTheirComments() throws Exception {
            when(showService.searchShows("girls")).thenReturn(List.of(
                    new ShowSummaryResponse(139L, "Girls", "HBO", "<p>Resumen</p>", List.of("Drama"),
                            List.of(new CommentResponse("Muy buena", 4))),
                    new ShowSummaryResponse(525L, "Gilmore Girls", "The CW", null, List.of(), List.of())));

            mockMvc.perform(get("/api/shows/search").param("q", "girls"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(139))
                    .andExpect(jsonPath("$[0].name").value("Girls"))
                    .andExpect(jsonPath("$[0].channel").value("HBO"))
                    .andExpect(jsonPath("$[0].genres[0]").value("Drama"))
                    .andExpect(jsonPath("$[0].comments[0].comment").value("Muy buena"))
                    .andExpect(jsonPath("$[0].comments[0].rating").value(4))
                    .andExpect(jsonPath("$[1].comments").isEmpty());
        }

        @Test
        void returns200WithEmptyArrayWhenThereAreNoResults() throws Exception {
            when(showService.searchShows("xyz")).thenReturn(List.of());

            mockMvc.perform(get("/api/shows/search").param("q", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void returns400WhenQueryIsMissing() throws Exception {
            mockMvc.perform(get("/api/shows/search"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value("El parámetro 'q' es obligatorio"));
            verifyNoInteractions(showService);
        }

        @Test
        void returns400WhenQueryIsBlank() throws Exception {
            mockMvc.perform(get("/api/shows/search").param("q", "   "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.q").value("no puede estar vacío"));
            verifyNoInteractions(showService);
        }

        @Test
        void returns502WhenTvMazeIsUnavailable() throws Exception {
            when(showService.searchShows(anyString())).thenThrow(new TvMazeUnavailableException(
                    "Error al consultar la búsqueda de shows en TV Maze", new RuntimeException()));

            mockMvc.perform(get("/api/shows/search").param("q", "girls"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.detail").value("Error al consultar la búsqueda de shows en TV Maze"));
        }

        @Test
        void returns503WhenMongoIsUnavailable() throws Exception {
            when(showService.searchShows(anyString())).thenThrow(new DataAccessResourceFailureException("timeout"));

            mockMvc.perform(get("/api/shows/search").param("q", "girls"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.detail").value("La base de datos no está disponible"));
        }
    }

    @Nested
    class GetShow {

        @Test
        void returns200WithFullShowAndComments() throws Exception {
            when(showService.getShow(139L)).thenReturn(Map.of(
                    "id", 139,
                    "name", "Girls",
                    "rating", Map.of("average", 6.5),
                    "comments", List.of(new CommentResponse("Muy buena", 4))));

            mockMvc.perform(get("/api/shows/139"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(139))
                    .andExpect(jsonPath("$.name").value("Girls"))
                    .andExpect(jsonPath("$.rating.average").value(6.5))
                    .andExpect(jsonPath("$.comments[0].comment").value("Muy buena"))
                    .andExpect(jsonPath("$.comments[0].rating").value(4));
        }

        @Test
        void returns400WhenShowIdIsNotPositive() throws Exception {
            mockMvc.perform(get("/api/shows/0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.showId").value("debe ser un número positivo"));
            verifyNoInteractions(showService);
        }

        @Test
        void returns400WhenShowIdIsNotANumber() throws Exception {
            mockMvc.perform(get("/api/shows/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El valor 'abc' no es válido para 'showId'"));
            verifyNoInteractions(showService);
        }

        @Test
        void returns404WhenShowDoesNotExist() throws Exception {
            when(showService.getShow(999L)).thenThrow(new ShowNotFoundException(999));

            mockMvc.perform(get("/api/shows/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value("No existe un show con ID 999"));
        }

        @Test
        void returns502WhenTvMazeIsUnavailable() throws Exception {
            when(showService.getShow(anyLong())).thenThrow(new TvMazeUnavailableException(
                    "Error al consultar el show 139 en TV Maze", new RuntimeException()));

            mockMvc.perform(get("/api/shows/139"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.detail").value("Error al consultar el show 139 en TV Maze"));
        }

        @Test
        void returns503WhenMongoIsUnavailable() throws Exception {
            when(showService.getShow(anyLong())).thenThrow(new DataAccessResourceFailureException("timeout"));

            mockMvc.perform(get("/api/shows/139"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.detail").value("La base de datos no está disponible"));
        }
    }
}
