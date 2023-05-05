package ru.practicum.shareit.item.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class WrongOwnerException extends ResponseStatusException {
    public WrongOwnerException(String message) {
        super(HttpStatus.FORBIDDEN, message);
        log.warn(message);
    }
}
