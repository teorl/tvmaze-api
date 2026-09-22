package com.evaluacion.tvmaze.controller;

import com.evaluacion.tvmaze.dto.ShowSummaryResponse;
import com.evaluacion.tvmaze.service.ShowService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    /**
     * Busca shows por nombre. Ejemplo: {@code GET /api/shows/search?q=girls}
     */
    @GetMapping("/search")
    public List<ShowSummaryResponse> search(@RequestParam("q") @NotBlank(message = "no puede estar vacío") String searchQuery) {
        return showService.searchShows(searchQuery);
    }

    /**
     * Devuelve el show completo de TV Maze. Ejemplo: {@code GET /api/shows/139}
     */
    @GetMapping("/{showId}")
    public Map<String, Object> getShow(@PathVariable @Positive(message = "debe ser un número positivo") long showId) {
        return showService.getShow(showId);
    }
}
