package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.service.BookingService;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingResponseDto create(
            @Valid @RequestBody BookingRequestDto bookingRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long bookerId) {
        return bookingMapper.toBookingResponseDto(bookingService.create(bookingMapper.toBooking(bookingRequestDto),
                bookerId));
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(
            @RequestParam Boolean approved, @PathVariable Long bookingId,
            @RequestHeader(USER_HEADER) @NotNull Long ownerId) {
        return bookingMapper.toBookingResponseDto(bookingService.approve(bookingId, ownerId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findById(
            @PathVariable Long bookingId,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return bookingMapper.toBookingResponseDto(bookingService.findByIdAndUserId(bookingId, userId));
    }

    @GetMapping
    public Collection<BookingResponseDto> findAllByUserIdAndState(
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "1000") int size) {

        return bookingService.findByUserIdAndState(userId, state, from, size).stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> findAllByOwnerIdAndState(
            @RequestHeader(USER_HEADER) @NotNull Long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "1000") int size) {

        return bookingService.findByOwnerIdAndState(ownerId, state, from, size).stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}
