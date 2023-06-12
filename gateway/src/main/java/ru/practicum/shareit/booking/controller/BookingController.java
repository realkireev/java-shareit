package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.cacheservice.BookingCacheService;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.common.Variables.USER_HEADER;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingCacheService bookingCacheService;

    @PostMapping
    public ResponseEntity<Object> create(
            @Valid @RequestBody BookingRequestDto bookingRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long bookerId) {

        return bookingCacheService.create(bookerId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @RequestParam Boolean approved,
            @PathVariable Long bookingId,
            @RequestHeader(USER_HEADER) @NotNull Long ownerId) {
        return bookingCacheService.approve(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findById(
            @PathVariable Long bookingId,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return bookingCacheService.findById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUserIdAndState(
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(name = "from", required = false, defaultValue = "0") int from,
            @Positive @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        return bookingCacheService.findAllByUserIdAndState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllByOwnerIdAndState(
            @RequestHeader(USER_HEADER) @NotNull Long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(name = "from", required = false, defaultValue = "0") int from,
            @Positive @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {
        return bookingCacheService.findAllByOwnerIdAndState(ownerId, state, from, size);
    }
}
