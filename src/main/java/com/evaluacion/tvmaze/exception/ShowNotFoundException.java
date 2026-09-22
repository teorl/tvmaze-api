package com.evaluacion.tvmaze.exception;

/**
 * Se lanza cuando TV Maze no tiene un show con el ID solicitado.
 */
public class ShowNotFoundException extends RuntimeException {

    public ShowNotFoundException(long showId) {
        super("No existe un show con ID %d".formatted(showId));
    }
}
