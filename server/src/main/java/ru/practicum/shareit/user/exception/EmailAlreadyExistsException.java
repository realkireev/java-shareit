package ru.practicum.shareit.user.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class EmailAlreadyExistsException extends ResponseStatusException {
    public EmailAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
        log.warn(message);
    }
}
