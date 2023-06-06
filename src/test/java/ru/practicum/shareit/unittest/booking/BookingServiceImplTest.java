package ru.practicum.shareit.unittest.booking;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingServiceImplTest {
    @Mock
    private BookingRepository mockBookingRepository;

    @Mock
    private ItemServiceImpl mockItemService;

    @Mock
    private UserServiceImpl mockUserService;

    @Mock
    private BookingMapper mockBookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private User owner;
    private User booker;
    private Item item;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;
    private Booking booking2;
    private List<Booking> bookings;
    private List<BookingResponseDto> bookingsDto;
    private BookingResponseDto bookingResponseDto;
    private BookingResponseDto bookingResponseDto2;
    private Pageable pageable;
    private List<Booking> resultBookings;

    @Test
    public void shouldCreateBooking() {
        createTestObjects();

        when(mockItemService.findById(item.getId())).thenReturn(item);
        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockBookingMapper.toBooking(bookingRequestDto)).thenReturn(booking);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.create(bookingRequestDto, booker.getId());

        assertNotNull(result);
        assertEquals(result, bookingResponseDto);

        verify(mockItemService, times(1)).findById(booking.getItemId());
        verify(mockUserService, times(1)).findUserById(booker.getId());
        verify(mockBookingMapper, times(1)).toBooking(bookingRequestDto);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingRepository, times(1)).save(booking);
    }

    @Test
    public void shouldApproveBooking() {
        createTestObjects();

        when(mockBookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(mockItemService.findById(booking.getItemId())).thenReturn(item);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.approve(booking.getId(), owner.getId(), true);

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);

        verify(mockBookingRepository, times(1)).findById(booking.getId());
        verify(mockItemService, times(1)).findById(booking.getItemId());
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingRepository, times(1)).save(booking);
    }

    @Test
    public void shouldFindByIdAndUserId() {
        Long bookingId = 1L;
        Long userId = 1L;

        createTestObjects();

        when(mockBookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(mockItemService.findById(booking.getItemId())).thenReturn(item);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.findByIdAndUserId(bookingId, userId);

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);

        verify(mockBookingRepository, times(1)).findById(bookingId);
        verify(mockItemService, times(1)).findById(booking.getItemId());
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
    }

    @Test
    public void shouldFindById() {
        Long bookingId = 1L;
        createTestObjects();

        when(mockBookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.findById(bookingId);

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);

        verify(mockBookingRepository, times(1)).findById(bookingId);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);

    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateALL() {
        Long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 20;

        createTestObjects();
        Pageable pageable = PageRequest.of(0, size, SORT_BY_START_DESC);
        List<Booking> resultBookings = new ArrayList<>(bookings);

        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockBookingRepository.findByBookerId(userId, pageable)).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByBookerId(userId, pageable);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateFUTURE() {
        Long userId = 1L;
        String state = "FUTURE";
        int from = 0;
        int size = 20;

        createTestObjects();
        Pageable pageable = PageRequest.of(0, size, SORT_BY_START_DESC);
        List<Booking> resultBookings = new ArrayList<>(bookings);

        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockBookingRepository.findByBookerIdAndStartIsAfter(eq(userId), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByBookerIdAndStartIsAfter(eq(userId),
                any(LocalDateTime.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStatePAST() {
        Long userId = 1L;
        String state = "PAST";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockBookingRepository.findByBookerIdAndEndIsBefore(eq(userId), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByBookerIdAndEndIsBefore(eq(userId),
                any(LocalDateTime.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateCURRENT() {
        Long userId = 1L;
        String state = "CURRENT";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockBookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(eq(userId), any(LocalDateTime.class),
                any(LocalDateTime.class), eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByBookerIdAndStartIsBeforeAndEndIsAfter(eq(userId),
                any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateWAITING() {
        Long userId = 1L;
        String state = "WAITING";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockBookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(userId), any(BookingStatus.class),
                eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByBookerIdAndStatusOrderByStartDesc(eq(userId),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateREJECTED() {
        Long userId = 1L;
        String state = "REJECTED";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockBookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(userId), any(BookingStatus.class),
                eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByBookerIdAndStatusOrderByStartDesc(eq(userId),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateALL() {
        Long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 20;

        createTestObjects();
        Pageable pageable = PageRequest.of(0, size, SORT_BY_START_DESC);
        List<Booking> resultBookings = new ArrayList<>(bookings);

        when(mockUserService.findUserById(owner.getId())).thenReturn(owner);
        when(mockBookingRepository.findByOwnerId(owner, pageable)).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByOwnerId(owner, pageable);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateFUTURE() {
        Long userId = 1L;
        String state = "FUTURE";
        int from = 0;
        int size = 20;

        createTestObjects();
        Pageable pageable = PageRequest.of(0, size, SORT_BY_START_DESC);
        List<Booking> resultBookings = new ArrayList<>(bookings);

        when(mockUserService.findUserById(owner.getId())).thenReturn(owner);
        when(mockBookingRepository.findByOwnerIdInFuture(eq(owner), eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByOwnerIdInFuture(eq(owner), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStatePAST() {
        Long userId = 1L;
        String state = "PAST";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(owner.getId())).thenReturn(owner);
        when(mockBookingRepository.findByOwnerIdInPast(eq(owner), eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByOwnerIdInPast(eq(owner), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateCURRENT() {
        Long userId = 1L;
        String state = "CURRENT";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(owner.getId())).thenReturn(owner);
        when(mockBookingRepository.findByOwnerIdInCurrent(eq(owner), eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByOwnerIdInCurrent(eq(owner), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateWAITING() {
        Long userId = 1L;
        String state = "WAITING";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(owner.getId())).thenReturn(owner);
        when(mockBookingRepository.findByOwnerIdAndStatus(eq(owner), any(BookingStatus.class),
                eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByOwnerIdAndStatus(eq(owner),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateREJECTED() {
        Long userId = 1L;
        String state = "REJECTED";
        int from = 0;
        int size = 20;

        createTestObjects();

        when(mockUserService.findUserById(owner.getId())).thenReturn(owner);
        when(mockBookingRepository.findByOwnerIdAndStatus(eq(owner), any(BookingStatus.class),
                eq(pageable))).thenReturn(resultBookings);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());
        assertEquals(bookingsDto.get(0), result.get(0));
        assertEquals(bookingsDto.get(1), result.get(1));

        verify(mockBookingRepository, times(1)).findByOwnerIdAndStatus(eq(owner),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnLastBooking() {
        Long itemId = 1L;
        createTestObjects();

        Optional<Booking> optionalBooking = Optional.of(booking);

        when(mockBookingRepository.findLastBookingByItemId(itemId)).thenReturn(optionalBooking);
        Booking result = bookingService.findLastBookingByItemId(itemId);

        assertNotNull(result);
        assertEquals(booking, result);

        verify(mockBookingRepository, times(1)).findLastBookingByItemId(itemId);
    }

    @Test
    public void shouldReturnNextBooking() {
        Long itemId = 1L;
        createTestObjects();

        Optional<Booking> optionalBooking = Optional.of(booking); // Provide appropriate booking object

        when(mockBookingRepository.findNextBookingByItemId(itemId)).thenReturn(optionalBooking);

        Booking result = bookingService.findNextBookingByItemId(itemId);

        assertNotNull(result);
        assertEquals(booking, result);

        verify(mockBookingRepository, times(1)).findNextBookingByItemId(itemId);
    }

    @Test
    public void shouldReturnHasUserBookedItem() {
        Long userId = 1L;
        Long itemId = 1L;
        createTestObjects();

        when(mockBookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatus(eq(userId), eq(itemId), any(LocalDateTime.class),
                eq(BookingStatus.APPROVED))).thenReturn(bookings);

        Boolean result = bookingService.hasUserBookedItem(userId, itemId);

        assertNotNull(result);
        assertTrue(result);

        verify(mockBookingRepository, times(1)).findByBookerIdAndItemIdAndEndIsBeforeAndStatus(eq(userId), eq(itemId),
                any(LocalDateTime.class), eq(BookingStatus.APPROVED));
    }

    private void createTestObjects() {
        owner = User.builder()
            .id(1L)
            .name("Bill Gates")
            .email("gates@microsoft.com")
            .build();

        booker = User.builder()
            .id(2L)
            .name("Timothy Sales")
            .email("sales@xerox.com")
            .build();

        item = Item.builder()
            .id(1L)
            .name("Simple tool")
            .description("Not provided")
            .owner(owner)
            .available(true)
            .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Screwdriver")
                .description("With a good rubber handle")
                .owner(owner)
                .available(true)
                .build();

        LocalDateTime start = LocalDateTime.now().plusSeconds(2);
        LocalDateTime end = LocalDateTime.now().plusSeconds(62);

        LocalDateTime start2 = start.plusHours(2);
        LocalDateTime end2 = end.plusHours(2);

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(start);
        bookingRequestDto.setEnd(end);

        booking = Booking.builder()
            .id(1L)
            .start(start)
            .end(end)
            .bookerId(booker.getId())
            .itemId(item.getId())
            .status(BookingStatus.WAITING)
            .build();

        booking2 = Booking.builder()
                .id(2L)
                .start(start.plusHours(1))
                .end(end.plusHours(1))
                .bookerId(booker.getId())
                .itemId(item2.getId())
                .status(BookingStatus.WAITING)
                .build();

        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(booker.getId())
                .name(booker.getName())
                .email(booker.getEmail())
                .build();

        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(1L)
                .available(true)
                .comments(Collections.emptyList())
                .build();

        ItemResponseDto itemResponseDto2 = ItemResponseDto.builder()
                .id(item2.getId())
                .name(item2.getName())
                .description(item2.getDescription())
                .requestId(2L)
                .available(true)
                .comments(Collections.emptyList())
                .build();

        bookingResponseDto = BookingResponseDto.builder()
            .id(1L)
            .booker(userResponseDto)
            .item(itemResponseDto)
            .start(start)
            .end(end)
            .status(BookingStatus.WAITING)
            .build();

        bookingResponseDto2 = BookingResponseDto.builder()
                .id(2L)
                .booker(userResponseDto)
                .item(itemResponseDto2)
                .start(start2)
                .end(end2)
                .status(BookingStatus.WAITING)
                .build();

        bookings = List.of(booking, booking2);
        bookingsDto = List.of(bookingResponseDto, bookingResponseDto2);

        pageable = PageRequest.of(0, 20, SORT_BY_START_DESC);
        resultBookings = new ArrayList<>(bookings);
    }
}