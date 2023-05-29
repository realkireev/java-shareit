package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.BookingWrongStateRequestedException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.RequestBookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserService userService,
                              @Lazy ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public Booking create(Booking booking, Long bookerId) {
        checkUserExists(bookerId);
        booking.setBookerId(bookerId);

        Item item = itemService.findById(booking.getItemId());
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ItemNotFoundException("Owner can't book the item he owns");
        }

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException(String.format("Item with id %d is not available", booking.getItemId()));
        }

        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long bookingId, Long ownerId, Boolean approved) {
        checkUserExists(ownerId);

        Booking booking = findById(bookingId);
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ItemNotAvailableException("The booking is not in WAITING status");
        }

        Long itemId = booking.getItemId();
        Item item = itemService.findById(itemId);

        Long storedOwnerId = item.getOwner().getId();
        if (!storedOwnerId.equals(ownerId)) {
            throw new ItemNotFoundException(String.format("Item with id %d does not belong to user with id %d", itemId,
                    ownerId));
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking findByIdAndUserId(Long bookingId, Long userId) {
        Booking booking = findById(bookingId);

        if (booking.getBookerId().equals(userId)) {
            // Returns booking only for the booker ...
            return booking;
        }

        Item item = itemService.findById(booking.getItemId());
        if (item.getOwner().getId().equals(userId)) {
            // ... or for the owner
            return booking;
        }

        throw new BookingNotFoundException(String.format("Booking with id %d not found for user with id %d", bookingId,
                userId));
    }

    @Override
    public Booking findById(Long bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id %d not found", bookingId));
        }

        return optionalBooking.get();
    }

    @Override
    public Collection<Booking> findByUserIdAndState(Long userId, String state) {
        checkUserExists(userId);

        // Catch illegal states
        RequestBookingState requestBookingState = getRequestBookingStateOrThrowException(state);

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
        User owner = userService.findById(ownerId);

        // Catch illegal states
        RequestBookingState requestBookingState = getRequestBookingStateOrThrowException(state);

        Collection<Booking> result = Collections.emptyList();
        switch (requestBookingState) {
            case ALL:
                result = bookingRepository.findByOwnerId(owner);
                break;

            case FUTURE:
                result = bookingRepository.findByOwnerIdInFuture(owner);
                break;

            case PAST:
                result = bookingRepository.findByOwnerIdInPast(owner);
                break;

            case CURRENT:
                result = bookingRepository.findByOwnerIdInCurrent(owner);
                break;

            case WAITING:
            case REJECTED:
                BookingStatus bookingStatus = BookingStatus.valueOf(state);

                result = bookingRepository.findByOwnerIdAndStatus(owner, bookingStatus);
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
        userService.findById(userId);
    }

    private RequestBookingState getRequestBookingStateOrThrowException(String state) {
        try {
            return RequestBookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingWrongStateRequestedException(String.format("Unknown state: %s", state));
        }
    }
}
