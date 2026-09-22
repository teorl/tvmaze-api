package com.evaluacion.tvmaze.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Traduce las excepciones de la aplicación a respuestas HTTP con formato RFC 7807 (ProblemDetail).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String INVALID_REQUEST_DETAIL = "La petición contiene campos inválidos";

    @ExceptionHandler(TvMazeUnavailableException.class)
    public ProblemDetail handleTvMazeUnavailable(TvMazeUnavailableException ex) {
        log.error(ex.getMessage(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(DataAccessException ex) {
        log.error("Error al acceder a MongoDB", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "La base de datos no está disponible");
    }

    @ExceptionHandler(ShowNotFoundException.class)
    public ProblemDetail handleShowNotFound(ShowNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El valor '%s' no es válido para '%s'".formatted(ex.getValue(), ex.getName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El parámetro '%s' es obligatorio".formatted(ex.getParameterName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o tiene tipos de dato incorrectos");
    }

    /**
     * Errores de validación cuando el controlador también valida parámetros de ruta o de query
     * (por ejemplo {@code @Positive}); en ese caso Spring valida el cuerpo {@code @Valid} en el mismo paso.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new TreeMap<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                addFieldErrors(errors, parameterErrors.getFieldErrors());
            } else {
                String parameterName = resolveParameterName(result.getMethodParameter());
                result.getResolvableErrors().forEach(error -> addError(errors, parameterName, error));
            }
        }
        return validationProblem(errors);
    }

    /**
     * Errores de validación de un cuerpo {@code @Valid} cuando no hay otras restricciones en el método.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new TreeMap<>();
        addFieldErrors(errors, ex.getBindingResult().getFieldErrors());
        return validationProblem(errors);
    }

    private static ProblemDetail validationProblem(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, INVALID_REQUEST_DETAIL);
        problem.setProperty("errors", errors);
        return problem;
    }

    private static void addFieldErrors(Map<String, String> errors, List<FieldError> fieldErrors) {
        fieldErrors.forEach(error -> addError(errors, error.getField(), error));
    }

    /**
     * Si un campo tiene varias violaciones, se concatenan sus mensajes.
     */
    private static void addError(Map<String, String> errors, String field, MessageSourceResolvable error) {
        errors.merge(field, error.getDefaultMessage(), (current, added) -> current + "; " + added);
    }

    /**
     * Usa el nombre expuesto en la URL ({@code @RequestParam("q")}) en lugar del nombre de la variable Java.
     */
    private static String resolveParameterName(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null && !requestParam.name().isEmpty()) {
            return requestParam.name();
        }
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null && !pathVariable.name().isEmpty()) {
            return pathVariable.name();
        }
        return parameter.getParameterName();
    }
}
