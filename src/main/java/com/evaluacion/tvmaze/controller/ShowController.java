package com.evaluacion.tvmaze.controller;

import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de consulta de shows: búsqueda por nombre y detalle por ID.
 */
@Tag(name = "Shows", description = "Búsqueda y detalle de shows de TV Maze")
@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private static final String SHOW_EXAMPLE = """
            {
              "id": 139,
              "url": "https://www.tvmaze.com/shows/139/girls",
              "name": "Girls",
              "type": "Scripted",
              "language": "English",
              "genres": ["Drama", "Romance"],
              "status": "Ended",
              "network": {"id": 8, "name": "HBO"},
              "comments": [{"comment": "Muy buena serie", "rating": 4}]
            }
            """;

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    /**
     * Busca shows por nombre. Ejemplo: {@code GET /api/shows/search?q=girls}
     */
    @Operation(summary = "Buscar shows por nombre",
            description = "Consulta la búsqueda de TV Maze y devuelve una versión reducida de cada show, "
                    + "con los comentarios guardados para cada uno.")
    @ApiResponse(responseCode = "200", description = "Resultados de la búsqueda (puede ser un arreglo vacío)")
    @ApiResponse(responseCode = "400", description = "Falta el parámetro q o está vacío",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "502", description = "TV Maze no respondió o respondió con error",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "MongoDB no está disponible",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/search")
    public List<ShowSummaryResponse> search(
            @Parameter(description = "Texto a buscar en el nombre del show", example = "girls")
            @RequestParam("q") @NotBlank(message = "no puede estar vacío") String searchQuery) {
        return showService.searchShows(searchQuery);
    }

    /**
     * Devuelve el show completo de TV Maze con sus comentarios. Ejemplo: {@code GET /api/shows/139}
     */
    @Operation(summary = "Obtener un show por ID",
            description = "Devuelve el objeto show completo de TV Maze, sin omitir campos, más el arreglo "
                    + "comments. El show se guarda en caché en MongoDB después de la primera consulta.")
    @ApiResponse(responseCode = "200", description = "Show completo con sus comentarios",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = SHOW_EXAMPLE)))
    @ApiResponse(responseCode = "400", description = "El showId no es un número entero positivo",
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
    @GetMapping("/{showId}")
    public Map<String, Object> getShow(
            @Parameter(description = "ID del show en TV Maze", example = "139")
            @PathVariable @Positive(message = "debe ser un número positivo") long showId) {
        return showService.getShow(showId);
    }
}
