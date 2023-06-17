package ru.practicum.shareit.unittest.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.common.Variables.SORT_BY_START_DESC;

@SpringBootTest(classes = BookingServiceImplTest.class)
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

    private User owner;
    private User booker;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;
    private Booking booking2;
    private List<Booking> bookings;
    private List<BookingResponseDto> bookingsDto;
    private BookingResponseDto bookingResponseDto;
    private Pageable pageable;
    private final int from = 0;
    private final int size = 20;

    @BeforeEach
    public void preparation() {
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

        Item item = Item.builder()
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

        BookingResponseDto bookingResponseDto2 = BookingResponseDto.builder()
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

        when(mockItemService.findById(item.getId())).thenReturn(item);
        when(mockUserService.findUserById(booker.getId())).thenReturn(booker);
        when(mockUserService.findUserById(owner.getId())).thenReturn(owner);

        when(mockBookingMapper.toBooking(bookingRequestDto)).thenReturn(booking);
        when(mockBookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        when(mockBookingMapper.toBookingResponseDto(booking2)).thenReturn(bookingResponseDto2);

        when(mockBookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);
        when(mockBookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
    }

    @Test
    public void shouldCreateBooking() {
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

        when(mockBookingRepository.findByBookerId(userId, pageable)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
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

        when(mockBookingRepository.findByBookerIdAndStartIsAfter(eq(userId), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByBookerIdAndStartIsAfter(eq(userId),
                any(LocalDateTime.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStatePAST() {
        Long userId = 1L;
        String state = "PAST";

        when(mockBookingRepository.findByBookerIdAndEndIsBefore(eq(userId), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByBookerIdAndEndIsBefore(eq(userId),
                any(LocalDateTime.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateCURRENT() {
        Long userId = 1L;
        String state = "CURRENT";

        when(mockBookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(eq(userId), any(LocalDateTime.class),
                any(LocalDateTime.class), eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByBookerIdAndStartIsBeforeAndEndIsAfter(eq(userId),
                any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateWAITING() {
        Long userId = 1L;
        String state = "WAITING";

        when(mockBookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(userId), any(BookingStatus.class),
                eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByBookerIdAndStatusOrderByStartDesc(eq(userId),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByUserIdAndStateREJECTED() {
        Long userId = 1L;
        String state = "REJECTED";

        when(mockBookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(userId), any(BookingStatus.class),
                eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByUserIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByBookerIdAndStatusOrderByStartDesc(eq(userId),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateALL() {
        Long userId = 1L;
        String state = "ALL";

        when(mockBookingRepository.findByOwnerId(owner, pageable)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByOwnerId(owner, pageable);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateFUTURE() {
        Long userId = 1L;
        String state = "FUTURE";

        when(mockBookingRepository.findByOwnerIdInFuture(eq(owner), eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByOwnerIdInFuture(eq(owner), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStatePAST() {
        Long userId = 1L;
        String state = "PAST";

        when(mockBookingRepository.findByOwnerIdInPast(eq(owner), eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByOwnerIdInPast(eq(owner), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateCURRENT() {
        Long userId = 1L;
        String state = "CURRENT";

        when(mockBookingRepository.findByOwnerIdInCurrent(eq(owner), eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByOwnerIdInCurrent(eq(owner), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateWAITING() {
        Long userId = 1L;
        String state = "WAITING";

        when(mockBookingRepository.findByOwnerIdAndStatus(eq(owner), any(BookingStatus.class),
                eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByOwnerIdAndStatus(eq(owner),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnBookingsByOwnerIdAndStateREJECTED() {
        Long userId = 1L;
        String state = "REJECTED";

        when(mockBookingRepository.findByOwnerIdAndStatus(eq(owner), any(BookingStatus.class),
                eq(pageable))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.findByOwnerIdAndState(userId, state, from, size);

        commonBookingsDtoAsserts(result);
        verify(mockBookingRepository, times(1)).findByOwnerIdAndStatus(eq(owner),
                any(BookingStatus.class), eq(pageable));
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking);
        verify(mockBookingMapper, times(1)).toBookingResponseDto(booking2);
    }

    @Test
    public void shouldReturnLastBooking() {
        Long itemId = 1L;

        when(mockBookingRepository.findLastBookingByItemId(itemId)).thenReturn(Optional.of(booking));
        Booking result = bookingService.findLastBookingByItemId(itemId);

        assertNotNull(result);
        assertEquals(booking, result);

        verify(mockBookingRepository, times(1)).findLastBookingByItemId(itemId);
    }

    @Test
    public void shouldReturnNextBooking() {
        Long itemId = 1L;

        when(mockBookingRepository.findNextBookingByItemId(itemId)).thenReturn(Optional.of(booking));

        Booking result = bookingService.findNextBookingByItemId(itemId);

        assertNotNull(result);
        assertEquals(booking, result);

        verify(mockBookingRepository, times(1)).findNextBookingByItemId(itemId);
    }

    @Test
    public void shouldReturnHasUserBookedItem() {
        Long userId = 1L;
        Long itemId = 1L;

        when(mockBookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatusOrderById(eq(userId), eq(itemId),
                any(LocalDateTime.class), eq(BookingStatus.APPROVED))).thenReturn(bookings);

        Boolean result = bookingService.hasUserBookedItem(userId, itemId);

        assertNotNull(result);
        assertTrue(result);

        verify(mockBookingRepository, times(1)).findByBookerIdAndItemIdAndEndIsBeforeAndStatusOrderById(eq(userId), eq(itemId),
                any(LocalDateTime.class), eq(BookingStatus.APPROVED));
    }

    private void commonBookingsDtoAsserts(List<BookingResponseDto> result) {
        assertNotNull(result);
        assertEquals(bookingsDto.size(), result.size());

        IntStream.range(0, result.size()).forEach(i -> assertEquals(bookingsDto.get(i), result.get(i)));
    }
}
