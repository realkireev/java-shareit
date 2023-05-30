package ru.practicum.shareit.pagination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class InvalidPaginationException extends ResponseStatusException {
    public InvalidPaginationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
        log.warn(message);
    }
}
