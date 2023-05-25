package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.RequestBookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repo.UserRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public Booking create(Booking booking, Long bookerId) {
        checkUserExists(bookerId);
        booking.setBookerId(bookerId);

        Optional<Item> item = itemRepository.findById(booking.getItemId());
        if (item.isEmpty() || item.get().getOwner().getId().equals(bookerId)) {
            throw new ItemNotFoundException(String.format("Item with id %d not found", booking.getItemId()));
        }
        if (!item.get().getAvailable()) {
            throw new ItemNotAvailableException(String.format("Item with id %d is not available", booking.getItemId()));
        }

        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long bookingId, Long ownerId, Boolean approved) {
        checkUserExists(ownerId);

        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id %d not found", bookingId));
        }

        Booking updatedBooking = booking.get();
        if (!updatedBooking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ItemNotAvailableException("The booking is not in WAITING status");
        }

        Long itemId = updatedBooking.getItemId();
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item with id %d not found", booking.get().getItemId()));
        }

        Long storedOwnerId = item.get().getOwner().getId();
        if (!storedOwnerId.equals(ownerId)) {
            throw new ItemNotFoundException(String.format("Item with id %d does not belong to user with id %d", itemId, ownerId));
        }

        if (approved) {
            updatedBooking.setStatus(BookingStatus.APPROVED);
        } else {
            updatedBooking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(updatedBooking);
    }

    @Override
    public Booking findById(Long bookingId, Long userId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id %d not found", bookingId));
        }

        Booking booking = optionalBooking.get();

        if (booking.getBookerId().equals(userId)) {
            // Returns booking only for the booker ...
            return booking;
        }

        Optional<Item> item = itemRepository.findById(booking.getItemId());
        if (item.isPresent() && item.get().getOwner().getId().equals(userId)) {
            // ... or for the owner
            return booking;
        }

        throw new BookingNotFoundException(String.format("Booking with id %d not found for user with id %d", bookingId,
                userId));
    }

    @Override
    public Collection<Booking> findByUserIdAndState(Long userId, String state) {
        checkUserExists(userId);

        // Catch illegal states
        RequestBookingState requestBookingState = getRequestBookingStateOrThrowException(state);
        if (requestBookingState == null) {
            return null;
        }

        Collection<Booking> result = Collections.emptyList();
        switch (requestBookingState) {
            case ALL:
                result = bookingRepository.findByBookerId(userId, SORT_BY_START_DESC);
                break;

            case FUTURE:
                result = bookingRepository.findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(),
                        SORT_BY_START_DESC);
                break;

            case PAST:
                result = bookingRepository.findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(),
                        SORT_BY_START_DESC);
                break;

            case CURRENT:
                result = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), SORT_BY_START_DESC);
                break;

            case WAITING:
            case REJECTED:
                BookingStatus bookingStatus = BookingStatus.valueOf(state);
                result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, bookingStatus,
                        SORT_BY_START_DESC);
                break;
        }

        return result;
    }

    @Override
    public Collection<Booking> findByOwnerIdAndState(Long ownerId, String state) {
        checkUserExists(ownerId);

        // Catch illegal states
        RequestBookingState requestBookingState = getRequestBookingStateOrThrowException(state);
        if (requestBookingState == null) {
            return null;
        }

        Collection<Booking> result = Collections.emptyList();
        switch (requestBookingState) {
            case ALL:
                result = bookingRepository.findByOwnerId(ownerId);
                break;

            case FUTURE:
                result = bookingRepository.findByOwnerIdInFuture(ownerId);
                break;

            case PAST:
                result = bookingRepository.findByOwnerIdInPast(ownerId);
                break;

            case CURRENT:
                result = bookingRepository.findByOwnerIdInCurrent(ownerId);
                break;

            case WAITING:
            case REJECTED:
                BookingStatus bookingStatus = BookingStatus.valueOf(state);

                result = bookingRepository.findByOwnerIdAndStatus(ownerId, bookingStatus);
                break;
        }

        return result;
    }

    @Override
    public Booking findLastBookingByItemId(Long itemId) {
        Optional<Booking> booking = bookingRepository.findLastBookingByItemId(itemId);
        return booking.orElse(null);
    }

    @Override
    public Booking findNextBookingByItemId(Long itemId) {
        Optional<Booking> booking = bookingRepository.findNextBookingByItemId(itemId);
        return booking.orElse(null);
    }

    public Boolean hasUserBookedItem(Long userId, Long itemId) {
        return bookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatus(userId, itemId, LocalDateTime.now(),
                BookingStatus.APPROVED).size() > 0;
    }

    private void checkUserExists(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }
    }

    private RequestBookingState getRequestBookingStateOrThrowException(String state) {
        try {
            return RequestBookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
