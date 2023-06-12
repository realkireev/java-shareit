package ru.practicum.shareit.item.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class ItemNotFoundException extends ResponseStatusException {
    public ItemNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
        log.warn(message);
    }
}
