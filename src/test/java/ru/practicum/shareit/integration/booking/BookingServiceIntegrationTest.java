package ru.practicum.shareit.integration.booking;

import org.junit.jupiter.api.BeforeEach;
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

    private Item unavailableItem;
    private BookingRequestDto bookingRequestDto;
    private UserResponseDto user1;
    private UserResponseDto user2;
    private ItemResponseDto itemResponseDto;

    @BeforeEach
    public void preparation() {
        UserRequestDto userRequestDto1 = new UserRequestDto();
        userRequestDto1.setName("Michael Tors");
        userRequestDto1.setEmail("tors@fashion.com");

        UserRequestDto userRequestDto2 = new UserRequestDto();
        userRequestDto2.setName("Bill Clinton");
        userRequestDto2.setEmail("potus@usa.gov");

        Item availableItem = Item.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop for gaming")
                .available(true)
                .build();

        unavailableItem = Item.builder()
                .id(2L)
                .name("Phone")
                .description("Latest smartphone with great camera")
                .available(false)
                .build();

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(availableItem.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        user1 = userService.create(userRequestDto1);
        user2 = userService.create(userRequestDto2);

        itemService.create(availableItem, user1.getId());
        itemService.create(unavailableItem, user1.getId());

        itemResponseDto = itemService.create(availableItem, user1.getId());
    }

    @Test
    public void shouldCreateBooking() {
        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());

        assertItemResponseDtoWithState(booking, BookingStatus.WAITING);
    }

    @Test
    public void shouldThrowExceptionWhenBookingByUnknownUser() {
        assertThrows(UserNotFoundException.class, () -> bookingService.create(bookingRequestDto, 99L));
    }

    @Test
    public void shouldThrowExceptionWhenBookingOfUnknownItem() {
        assertThrows(ItemNotFoundException.class, () -> bookingService.create(bookingRequestDto, user1.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenBookingByOwner() {
        assertThrows(ItemNotFoundException.class, () -> bookingService.create(bookingRequestDto, user1.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenItemUnavailable() {
        bookingRequestDto.setItemId(unavailableItem.getId());

        assertThrows(ItemNotAvailableException.class, () -> bookingService.create(bookingRequestDto, user2.getId()));
    }

    @Test
    public void shouldApproveBooking() {
        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());
        BookingResponseDto approvedBooking = bookingService.approve(booking.getId(), user1.getId(), true);

        assertItemResponseDtoWithState(approvedBooking, BookingStatus.APPROVED);
    }

    @Test
    public void shouldRejectBooking() {
        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());
        BookingResponseDto approvedBooking = bookingService.approve(booking.getId(), user1.getId(), false);

        assertItemResponseDtoWithState(approvedBooking, BookingStatus.REJECTED);
    }

    @Test
    public void shouldThrowExceptionWhenApproveByUnknownUser() {
        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());

        assertThrows(UserNotFoundException.class, () -> bookingService.approve(booking.getId(), 99L, true));
    }

    @Test
    public void shouldThrowExceptionWhenApproveNotOwner() {
        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());

        assertThrows(ItemNotFoundException.class, () -> bookingService.approve(booking.getId(), user2.getId(), true));
    }

    @Test
    public void shouldThrowExceptionWhenApproveBookingNotWAITING() {
        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());
        BookingResponseDto approvedBooking = bookingService.approve(booking.getId(), user1.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
        assertThrows(ItemNotAvailableException.class, () -> bookingService.approve(booking.getId(), user1.getId(), true));
    }

    private void assertItemResponseDtoWithState(BookingResponseDto result, BookingStatus state) {
        assertNotNull(result);
        assertEquals(itemResponseDto.getId(), result.getItem().getId());
        assertEquals(itemResponseDto.getName(), result.getItem().getName());
        assertEquals(itemResponseDto.getDescription(), result.getItem().getDescription());
        assertEquals(user2.getId(), result.getBooker().getId());
        assertEquals(state, result.getStatus());
    }
}
