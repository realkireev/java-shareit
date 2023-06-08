package ru.practicum.shareit.request.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class RequestNotFoundException extends ResponseStatusException {
    public RequestNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
        log.warn(message);
    }
}
