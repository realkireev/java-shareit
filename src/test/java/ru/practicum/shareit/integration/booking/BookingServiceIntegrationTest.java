package ru.practicum.shareit.integration.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private UserRequestDto userRequestDto1;
    private UserRequestDto userRequestDto2;
    private Item item1;
    private Item item2;

    @Test
    public void shouldCreateBooking() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(3));

        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);
        ItemResponseDto itemResponseDto = itemService.create(item1, user1.getId());

        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());

        assertNotNull(booking);
        assertEquals(itemResponseDto.getId(), booking.getItem().getId());
        assertEquals(itemResponseDto.getName(), booking.getItem().getName());
        assertEquals(itemResponseDto.getDescription(), booking.getItem().getDescription());
        assertEquals(user2.getId(), booking.getBooker().getId());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenBookingByUnknownUser() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        assertThrows(UserNotFoundException.class, () -> bookingService.create(bookingRequestDto, 99L));
    }

    @Test
    public void shouldThrowExceptionWhenBookingOfUnknownItem() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(99L);
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));
        UserResponseDto user1 = userService.create(userRequestDto1);

        assertThrows(ItemNotFoundException.class, () -> bookingService.create(bookingRequestDto, user1.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenBookingByOwner() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        UserResponseDto user1 = userService.create(userRequestDto1);

        assertThrows(ItemNotFoundException.class, () -> bookingService.create(bookingRequestDto, user1.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenItemUnavailable() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item2.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);
        itemService.create(item1, user1.getId());
        itemService.create(item2, user1.getId());

        assertThrows(ItemNotAvailableException.class, () -> bookingService.create(bookingRequestDto, user2.getId()));
    }

    @Test
    public void shouldApproveBooking() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);
        ItemResponseDto itemResponseDto = itemService.create(item1, user1.getId());

        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());
        BookingResponseDto approvedBooking = bookingService.approve(booking.getId(), user1.getId(), true);

        assertNotNull(approvedBooking);
        assertEquals(itemResponseDto.getId(), approvedBooking.getItem().getId());
        assertEquals(itemResponseDto.getName(), approvedBooking.getItem().getName());
        assertEquals(itemResponseDto.getDescription(), approvedBooking.getItem().getDescription());
        assertEquals(user2.getId(), approvedBooking.getBooker().getId());
        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    public void shouldRejectBooking() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);
        ItemResponseDto itemResponseDto = itemService.create(item1, user1.getId());

        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());
        BookingResponseDto approvedBooking = bookingService.approve(booking.getId(), user1.getId(), false);

        assertNotNull(approvedBooking);
        assertEquals(itemResponseDto.getId(), approvedBooking.getItem().getId());
        assertEquals(itemResponseDto.getName(), approvedBooking.getItem().getName());
        assertEquals(itemResponseDto.getDescription(), approvedBooking.getItem().getDescription());
        assertEquals(user2.getId(), approvedBooking.getBooker().getId());
        assertEquals(BookingStatus.REJECTED, approvedBooking.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenApproveByUnknownUser() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);
        itemService.create(item1, user1.getId());

        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());

        assertThrows(UserNotFoundException.class, () -> bookingService.approve(booking.getId(), 99L, true));
    }

    @Test
    public void shouldThrowExceptionWhenApproveNotOwner() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);
        itemService.create(item1, user1.getId());

        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());

        assertThrows(ItemNotFoundException.class, () -> bookingService.approve(booking.getId(), user2.getId(), true));
    }

    @Test
    public void shouldThrowExceptionWhenApproveBookingNotWAITING() {
        createTestObjects();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);
        itemService.create(item1, user1.getId());

        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());
        BookingResponseDto approvedBooking = bookingService.approve(booking.getId(), user1.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
        assertThrows(ItemNotAvailableException.class, () -> bookingService.approve(booking.getId(), user1.getId(), true));
    }

    private void createTestObjects() {
        userRequestDto1 = new UserRequestDto();
        userRequestDto1.setName("Michael Tors");
        userRequestDto1.setEmail("tors@fashion.com");

        userRequestDto2 = new UserRequestDto();
        userRequestDto2.setName("Bill Clinton");
        userRequestDto2.setEmail("potus@usa.gov");

        item1 = Item.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop for gaming")
                .available(true)
                .build();

        item2 = Item.builder()
                .id(2L)
                .name("Phone")
                .description("Latest smartphone with great camera")
                .available(false)
                .build();
    }
}
