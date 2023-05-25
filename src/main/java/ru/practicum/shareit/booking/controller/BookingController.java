package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.ErrorResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingController {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingDto create(@Valid @RequestBody Booking booking,
                             @RequestHeader(USER_HEADER) @NotNull Long bookerId) {
        return bookingMapper.toBookingDto(bookingService.create(booking, bookerId));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestParam Boolean approved, @PathVariable Long bookingId,
                              @RequestHeader(USER_HEADER) @NotNull Long ownerId) {
        return bookingMapper.toBookingDto(bookingService.approve(bookingId, ownerId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable Long bookingId, @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return bookingMapper.toBookingDto(bookingService.findById(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<?> findAllByUserIdAndState(@RequestHeader(USER_HEADER) @NotNull Long userId,
                                                     @RequestParam(defaultValue = "ALL") String state) {
        Collection<Booking> result = bookingService.findByUserIdAndState(userId, state);

        if (result == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(String.format("Unknown state: %s", state)));
        }

        return ResponseEntity.ok(result.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList()));
    }

    @GetMapping("/owner")
    public ResponseEntity<?> findAllByOwnerIdAndState(@RequestHeader(USER_HEADER) @NotNull Long ownerId,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        Collection<Booking> result = bookingService.findByOwnerIdAndState(ownerId, state);

        if (result == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(String.format("Unknown state: %s", state)));
        }

        return ResponseEntity.ok(result.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList()));
    }
}
