package com.evaluacion.tvmaze.controller;

import com.evaluacion.tvmaze.dto.CreateCommentRequest;
import com.evaluacion.tvmaze.dto.StatusResponse;
import com.evaluacion.tvmaze.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint para registrar la calificación y el comentario de un show.
 */
@Tag(name = "Comentarios", description = "Calificaciones y comentarios de los shows")
@RestController
@RequestMapping("/api/shows/{showId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Guarda la calificación y el comentario de un show. Ejemplo: {@code POST /api/shows/139/comments}
     */
    @Operation(summary = "Comentar y calificar un show",
            description = "Verifica que el show exista en TV Maze (usando el caché) y guarda el comentario "
                    + "en la colección comments.")
    @ApiResponse(responseCode = "201", description = "Comentario guardado")
    @ApiResponse(responseCode = "400",
            description = "El showId no es positivo, el cuerpo no es JSON válido o no cumple las validaciones; "
                    + "el detalle por campo viene en errors",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "TV Maze no tiene un show con ese ID",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "502", description = "TV Maze no respondió o respondió con error",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "MongoDB no está disponible",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatusResponse addComment(
            @Parameter(description = "ID del show en TV Maze", example = "139")
            @PathVariable @Positive(message = "debe ser un número positivo") long showId,
            @Valid @RequestBody CreateCommentRequest request) {
        String commentId = commentService.addComment(showId, request);
        return StatusResponse.success("Comentario guardado", commentId);
    }
}
