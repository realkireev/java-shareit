package ru.practicum.shareit.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class BookingExceptionHandler {
    @ExceptionHandler({ConstraintViolationException.class, BookingWrongStateRequestedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBookingWrongStateException(final Exception e) {
        return Map.of("error", Objects.requireNonNull(e.getMessage()));
    }
}
