package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.pagination.PaginationValidator;
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
    public BookingServiceImpl(
            BookingRepository bookingRepository,
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
    public Collection<Booking> findByUserIdAndState(Long userId, String state, int from, int size) {
        checkUserExists(userId);

        // Catch illegal states
        RequestBookingState requestBookingState = getRequestBookingStateOrThrowException(state);

        Collection<Booking> result = Collections.emptyList();

        PaginationValidator.validate(from, size);
        int page = from / size;

        Pageable pageable = PageRequest.of(page, size, SORT_BY_START_DESC);

        switch (requestBookingState) {
            case ALL:
                result = bookingRepository.findByBookerId(userId, pageable).getContent();
                break;

            case FUTURE:
                result = bookingRepository.findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), pageable).getContent();
                break;

            case PAST:
                result = bookingRepository.findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), pageable).getContent();
                break;

            case CURRENT:
                result = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), pageable).getContent();
                break;

            case WAITING:
            case REJECTED:
                BookingStatus bookingStatus = BookingStatus.valueOf(state);
                result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, bookingStatus,
                        pageable).getContent();
                break;
        }

        return result;
    }

    @Override
    public Collection<Booking> findByOwnerIdAndState(Long ownerId, String state, int from, int size) {
        User owner = checkUserExists(ownerId);

        // Catch illegal states
        RequestBookingState requestBookingState = getRequestBookingStateOrThrowException(state);

        Collection<Booking> result = Collections.emptyList();

        PaginationValidator.validate(from, size);
        int page = from / size;

        Pageable pageable = PageRequest.of(page, size, SORT_BY_START_DESC);

        switch (requestBookingState) {
            case ALL:
                result = bookingRepository.findByOwnerId(owner, pageable).getContent();
                break;

            case FUTURE:
                result = bookingRepository.findByOwnerIdInFuture(owner, pageable).getContent();
                break;

            case PAST:
                result = bookingRepository.findByOwnerIdInPast(owner, pageable).getContent();
                break;

            case CURRENT:
                result = bookingRepository.findByOwnerIdInCurrent(owner, pageable).getContent();
                break;

            case WAITING:
            case REJECTED:
                BookingStatus bookingStatus = BookingStatus.valueOf(state);

                result = bookingRepository.findByOwnerIdAndStatus(owner, bookingStatus, pageable).getContent();
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

    private User checkUserExists(Long userId) {
        return userService.findById(userId);
    }

    private RequestBookingState getRequestBookingStateOrThrowException(String state) {
        try {
            return RequestBookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingWrongStateRequestedException(String.format("Unknown state: %s", state));
        }
    }
}
