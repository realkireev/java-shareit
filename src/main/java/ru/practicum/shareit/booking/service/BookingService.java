package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import java.util.Collection;

public interface BookingService {
    Booking create(Booking booking, Long bookerId);

    Booking approve(Long bookingId, Long ownerId, Boolean approved);

    Booking findById(Long bookingId, Long userId);

    Collection<Booking> findByUserIdAndState(Long userId, String state);

    Collection<Booking> findByOwnerIdAndState(Long ownerId, String state);

    Booking findLastBookingByItemId(Long itemId);

    Booking findNextBookingByItemId(Long itemId);

    Boolean hasUserBookedItem(Long userId, Long itemId);
}