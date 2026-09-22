package com.evaluacion.tvmaze.exception;

/**
 * Se lanza cuando el API de TV Maze no responde o responde con error.
 */
public class TvMazeUnavailableException extends RuntimeException {

    public TvMazeUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
