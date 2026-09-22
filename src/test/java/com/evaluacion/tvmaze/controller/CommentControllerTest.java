package com.evaluacion.tvmaze.controller;

import com.evaluacion.tvmaze.dto.CreateCommentRequest;
import com.evaluacion.tvmaze.exception.ShowNotFoundException;
import com.evaluacion.tvmaze.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void returns201WithStatusWhenCommentIsSaved() throws Exception {
        when(commentService.addComment(139L, new CreateCommentRequest("Muy buena", 4))).thenReturn("abc123");

        mockMvc.perform(post("/api/shows/139/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Muy buena", "rating": 4}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Comentario guardado"))
                .andExpect(jsonPath("$.id").value("abc123"));
    }

    @Test
    void returns400WithFieldErrorsWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/shows/139/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "  ", "rating": 7}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La petición contiene campos inválidos"))
                .andExpect(jsonPath("$.errors.comment").value("es obligatorio y no puede estar vacío"))
                .andExpect(jsonPath("$.errors.rating").value("debe ser menor o igual a 5"));
        verifyNoInteractions(commentService);
    }

    @Test
    void returns400WhenRatingIsMissing() throws Exception {
        mockMvc.perform(post("/api/shows/139/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Muy buena"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").value("es obligatorio"));
    }

    @Test
    void returns400WhenRatingIsNotAnInteger() throws Exception {
        mockMvc.perform(post("/api/shows/139/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Muy buena", "rating": 4.5}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(commentService);
    }

    @Test
    void returns400WhenShowIdIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/shows/0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Muy buena", "rating": 4}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.showId").value("debe ser un número positivo"));
        verifyNoInteractions(commentService);
    }

    @Test
    void returns404WhenShowDoesNotExist() throws Exception {
        when(commentService.addComment(anyLong(), any())).thenThrow(new ShowNotFoundException(999));

        mockMvc.perform(post("/api/shows/999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Muy buena", "rating": 4}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un show con ID 999"));
        verify(commentService).addComment(anyLong(), any());
    }
}
