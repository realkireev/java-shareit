package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.RequestBookingState;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.common.MethodInfo;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.common.Variables.SORT_BY_START_DESC;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;
    private final Map<MethodInfo, List<BookingResponseDto>> cache = new HashMap<>();

    @Autowired
    public BookingServiceImpl(
            BookingRepository bookingRepository,
            UserService userService,
            @Lazy ItemService itemService,
            @Lazy BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public BookingResponseDto create(BookingRequestDto bookingRequestDto, Long bookerId) {
        checkUserExists(bookerId);

        Booking booking = bookingMapper.toBooking(bookingRequestDto);
        booking.setBookerId(bookerId);

        Item item = itemService.findById(booking.getItemId());
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ItemNotFoundException("Owner can't book the item he owns");
        }

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException(String.format("Item with id %d is not available", booking.getItemId()));
        }

        booking.setStatus(BookingStatus.WAITING);
        cache.clear();
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approve(Long bookingId, Long ownerId, Boolean approved) {
        checkUserExists(ownerId);

        Booking booking = findBookingById(bookingId);
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

        cache.clear();
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto findByIdAndUserId(Long bookingId, Long userId) {
        MethodInfo methodInfo = new MethodInfo("findByIdAndUserId", bookingId, userId);
        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo).get(0);
        }

        Booking booking = findBookingById(bookingId);
        BookingResponseDto bookingResponseDto = bookingMapper.toBookingResponseDto(booking);
        BookingResponseDto result = null;

        if (booking.getBookerId().equals(userId)) {
            // Returns booking only for the booker ...
            result = bookingResponseDto;
        }

        Item item = itemService.findById(booking.getItemId());
        if (item.getOwner().getId().equals(userId)) {
            // ... or for the owner
            result = bookingResponseDto;
        }

        if (result == null) {
            throw new BookingNotFoundException(String.format("Booking with id %d not found for user with id %d",
                    bookingId, userId));
        }

        cache.put(methodInfo, List.of(result));
        return result;
    }

    @Override
    public BookingResponseDto findById(Long bookingId) {
        MethodInfo methodInfo = new MethodInfo("findById", bookingId, bookingId);
        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo).get(0);
        }

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id %d not found", bookingId));
        }

        BookingResponseDto result = bookingMapper.toBookingResponseDto(optionalBooking.get());
        cache.put(methodInfo, List.of(result));

        return result;
    }

    @Override
    public List<BookingResponseDto> findByUserIdAndState(Long userId, String state, int from, int size) {
        checkUserExists(userId);

        MethodInfo methodInfo = new MethodInfo("findByUserIdAndState", userId, state, from, size);
        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo);
        }

        List<Booking> bookingList = Collections.emptyList();

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, SORT_BY_START_DESC);

        switch (RequestBookingState.valueOf(state)) {
            case ALL:
                bookingList = bookingRepository.findByBookerId(userId, pageable);
                break;

            case FUTURE:
                bookingList = bookingRepository.findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), pageable);
                break;

            case PAST:
                bookingList = bookingRepository.findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), pageable);
                break;

            case CURRENT:
                bookingList = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), pageable);
                break;

            case WAITING:
            case REJECTED:
                BookingStatus bookingStatus = BookingStatus.valueOf(state);
                bookingList = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, bookingStatus, pageable);
                break;
        }

        List<BookingResponseDto> result = bookingList.stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());

        cache.put(methodInfo, result);
        return result;
    }

    @Override
    public List<BookingResponseDto> findByOwnerIdAndState(Long ownerId, String state, int from, int size) {
        User owner = checkUserExists(ownerId);

        MethodInfo methodInfo = new MethodInfo("findByOwnerIdAndState", ownerId, state, from, size);
        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo);
        }

        List<Booking> bookingList = Collections.emptyList();

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, SORT_BY_START_DESC);

        switch (RequestBookingState.valueOf(state)) {
            case ALL:
                bookingList = bookingRepository.findByOwnerId(owner, pageable);
                break;

            case FUTURE:
                bookingList = bookingRepository.findByOwnerIdInFuture(owner, pageable);
                break;

            case PAST:
                bookingList = bookingRepository.findByOwnerIdInPast(owner, pageable);
                break;

            case CURRENT:
                bookingList = bookingRepository.findByOwnerIdInCurrent(owner, pageable);
                break;

            case WAITING:
            case REJECTED:
                BookingStatus bookingStatus = BookingStatus.valueOf(state);

                bookingList = bookingRepository.findByOwnerIdAndStatus(owner, bookingStatus, pageable);
                break;
        }

        List<BookingResponseDto> result = bookingList.stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());

        cache.put(methodInfo, result);
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
        return bookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatusOrderById(userId, itemId, LocalDateTime.now(),
                BookingStatus.APPROVED).size() > 0;
    }

    private User checkUserExists(Long userId) {
        return userService.findUserById(userId);
    }

    private Booking findBookingById(Long bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id %d not found", bookingId));
        }

        return optionalBooking.get();
    }
}
