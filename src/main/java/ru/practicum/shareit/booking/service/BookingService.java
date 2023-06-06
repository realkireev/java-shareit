package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingRequestDto bookingRequestDto, Long bookerId);

    BookingResponseDto approve(Long bookingId, Long ownerId, Boolean approved);

    BookingResponseDto findByIdAndUserId(Long bookingId, Long userId);

    BookingResponseDto findById(Long bookingId);

    List<BookingResponseDto> findByUserIdAndState(Long userId, String state, int from, int size);

    List<BookingResponseDto> findByOwnerIdAndState(Long ownerId, String state, int from, int size);

    Booking findLastBookingByItemId(Long itemId);

    Booking findNextBookingByItemId(Long itemId);

    Boolean hasUserBookedItem(Long userId, Long itemId);
}