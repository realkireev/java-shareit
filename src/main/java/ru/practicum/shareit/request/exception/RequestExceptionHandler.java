package ru.practicum.shareit.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class RequestExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleRequestNotFoundException(final RequestNotFoundException e) {
        return Map.of("error", Objects.requireNonNull(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolationException(final ConstraintViolationException e) {
        return Map.of("error", Objects.requireNonNull(e.getMessage()));
    }
}
