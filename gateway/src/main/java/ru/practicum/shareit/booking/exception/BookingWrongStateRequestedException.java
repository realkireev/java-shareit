package ru.practicum.shareit.booking.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookingWrongStateRequestedException extends RuntimeException {
    public BookingWrongStateRequestedException(String message) {
        super(message);
        log.warn(message);
    }
}
