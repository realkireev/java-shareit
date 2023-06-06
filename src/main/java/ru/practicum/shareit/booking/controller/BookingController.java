package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.Variables.USER_HEADER;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(
            @Valid @RequestBody BookingRequestDto bookingRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long bookerId) {
        return bookingService.create(bookingRequestDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(
            @RequestParam Boolean approved, @PathVariable Long bookingId,
            @RequestHeader(USER_HEADER) @NotNull Long ownerId) {
        return bookingService.approve(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findById(
            @PathVariable Long bookingId,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return bookingService.findByIdAndUserId(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> findAllByUserIdAndState(
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(name = "from", required = false, defaultValue = "0") int from,
            @Positive @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        return bookingService.findByUserIdAndState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findAllByOwnerIdAndState(
            @RequestHeader(USER_HEADER) @NotNull Long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(name = "from", required = false, defaultValue = "0") int from,
            @Positive @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {
        return bookingService.findByOwnerIdAndState(ownerId, state, from, size);
    }
}
