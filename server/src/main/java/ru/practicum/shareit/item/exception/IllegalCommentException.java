package ru.practicum.shareit.item.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class IllegalCommentException extends ResponseStatusException {
    public IllegalCommentException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
        log.warn(message);
    }
}
