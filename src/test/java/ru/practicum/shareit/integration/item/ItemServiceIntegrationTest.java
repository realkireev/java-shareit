package ru.practicum.shareit.integration.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.exception.IllegalCommentException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {
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
    private Item item3;
    private CommentRequestDto commentDto;

    @Test
    void shouldCreateItem() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);

        ItemResponseDto itemResponseDto = itemService.create(item1, user1.getId());

        assertNotNull(itemResponseDto);
        assertNotNull(itemResponseDto.getId());
        assertEquals(item1.getName(), itemResponseDto.getName());
        assertEquals(item1.getDescription(), itemResponseDto.getDescription());
        assertEquals(item1.getAvailable(), itemResponseDto.getAvailable());
    }

    @Test
    void shouldThrowExceptionWhenCreateItemForInvalidUser() {
        createTestObjects();
        assertThrows(UserNotFoundException.class, () -> itemService.create(item1, 123L));
    }

    @Test
    void shouldUpdateItem() {
        createTestObjects();

        UserResponseDto user1 = userService.create(userRequestDto1);
        ItemResponseDto createdItem = itemService.create(item1, user1.getId());
        ItemResponseDto responseDto = itemService.update(item2, createdItem.getId(), user1.getId());

        assertNotNull(responseDto);
        assertEquals(createdItem.getId(), responseDto.getId());
        assertEquals(item2.getName(), responseDto.getName());
        assertEquals(item2.getDescription(), responseDto.getDescription());
        assertEquals(item2.getAvailable(), responseDto.getAvailable());
    }

    @Test
    void shouldThrowExceptionWhenUpdateItemWithWrongUser() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);
        itemService.create(item1, user1.getId());
        assertThrows(ItemNotFoundException.class, () -> itemService.update(item2, 123L, 456L));
    }

    @Test
    void shouldFindItemById() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);
        ItemResponseDto createdItem = itemService.create(item1, user1.getId());

        Item foundItem = itemService.findById(createdItem.getId());

        assertNotNull(foundItem);
        assertEquals(createdItem.getId(), foundItem.getId());
        assertEquals(item1.getName(), foundItem.getName());
        assertEquals(item1.getDescription(), foundItem.getDescription());
        assertEquals(item1.getAvailable(), foundItem.getAvailable());
        assertEquals(user1.getId(), foundItem.getOwner().getId());
    }

    @Test
    void shouldThrowExceptionWhenFindItemByInvalidId() {
        assertThrows(ItemNotFoundException.class, () -> itemService.findById(123L));
    }

    @Test
    void shouldFindItemByOwnerId() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);

        itemService.create(item1, user1.getId());
        itemService.create(item2, user1.getId());

        List<ItemWithBookingsDto> items = itemService.findByOwnerId(user1.getId());

        assertNotNull(items);
        assertEquals(2, items.size());

        ItemWithBookingsDto itemDto1 = items.get(0);
        assertEquals(item1.getId(), itemDto1.getId());
        assertEquals(item1.getName(), itemDto1.getName());
        assertEquals(item1.getDescription(), itemDto1.getDescription());
        assertEquals(item1.getAvailable(), itemDto1.getAvailable());

        ItemWithBookingsDto itemDto2 = items.get(1);
        assertEquals(item2.getId(), itemDto2.getId());
        assertEquals(item2.getName(), itemDto2.getName());
        assertEquals(item2.getDescription(), itemDto2.getDescription());
        assertEquals(item2.getAvailable(), itemDto2.getAvailable());
    }

    @Test
    void shouldSearchItems() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);

        itemService.create(item1, user1.getId()); // does not match
        itemService.create(item2, user1.getId()); // unavailable
        itemService.create(item3, user1.getId()); // matches

        List<ItemResponseDto> searchResults = itemService.search("caMeRa");

        assertNotNull(searchResults);
        assertEquals(1, searchResults.size());

        assertEquals(item3.getId(), searchResults.get(0).getId());
        assertEquals(item3.getName(), searchResults.get(0).getName());
        assertEquals(item3.getDescription(), searchResults.get(0).getDescription());
        assertEquals(item3.getAvailable(), searchResults.get(0).getAvailable());
    }

    @Test
    void shouldDelete() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);

        itemService.create(item1, user1.getId());
        itemService.delete(item1.getId(), user1.getId());

        assertThrows(ItemNotFoundException.class, () -> itemService.findById(item1.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDeleteWithWrongUser() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);

        itemService.create(item1, user1.getId());
        assertThrows(WrongOwnerException.class, () -> itemService.delete(item1.getId(), 456L));
    }

    @Test
    void shouldThrowExceptionWhenDeleteWithWrongItemId() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);

        itemService.create(item1, user1.getId());
        assertThrows(ItemNotFoundException.class, () -> itemService.delete(123L, user1.getId()));
    }

    @Test
    void shouldFindByIdWithBookingsForOwner() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);
        itemService.create(item1, user1.getId());

        ItemWithBookingsDto itemWithBookingsDto = itemService.findByIdWithBookings(item1.getId(), user1.getId());

        assertNotNull(itemWithBookingsDto);
        assertEquals(item1.getId(), itemWithBookingsDto.getId());
        assertEquals(item1.getName(), itemWithBookingsDto.getName());
        assertEquals(item1.getDescription(), itemWithBookingsDto.getDescription());
        assertEquals(item1.getAvailable(), itemWithBookingsDto.getAvailable());
        assertNull(itemWithBookingsDto.getLastBooking());
        assertNull(itemWithBookingsDto.getNextBooking());
    }

    @Test
    void shouldFindByIdWithBookingsForNonOwner() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);

        itemService.create(item1, user1.getId());

        ItemWithBookingsDto itemWithBookingsDto = itemService.findByIdWithBookings(item1.getId(), user2.getId());

        assertNotNull(itemWithBookingsDto);
        assertEquals(item1.getId(), itemWithBookingsDto.getId());
        assertEquals(item1.getName(), itemWithBookingsDto.getName());
        assertEquals(item1.getDescription(), itemWithBookingsDto.getDescription());
        assertEquals(item1.getAvailable(), itemWithBookingsDto.getAvailable());
        assertNull(itemWithBookingsDto.getLastBooking());
        assertNull(itemWithBookingsDto.getNextBooking());
    }

    @Test
    void shouldThrowExceptionWhenAddCommentUserHasNeverBooked() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);

        itemService.create(item1, user1.getId());

        assertThrows(IllegalCommentException.class, () -> itemService.addComment(user1.getId(), item1.getId(),
                commentDto));
    }

    @Test
    void shouldAddComment() throws InterruptedException {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);
        UserResponseDto user2 = userService.create(userRequestDto2);

        itemService.create(item1, user1.getId());

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item1.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(2));

        BookingResponseDto booking = bookingService.create(bookingRequestDto, user2.getId());
        bookingService.approve(booking.getId(), user1.getId(), true);

        Thread.sleep(3000);

        CommentResponseDto comment = itemService.addComment(user2.getId(), item1.getId(), commentDto);

        assertEquals(commentDto.getText(), comment.getText());
    }

    @Test
    void shouldThrowExceptionWhenAddCommentByUnknownUser() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);
        itemService.create(item1, user1.getId());

        assertThrows(UserNotFoundException.class, () -> itemService.addComment(123L, item1.getId(), commentDto));
    }

    @Test
    void shouldThrowExceptionWhenAddCommentToInvalidItem() {
        createTestObjects();
        UserResponseDto user1 = userService.create(userRequestDto1);
        itemService.create(item1, user1.getId());

        assertThrows(ItemNotFoundException.class, () -> itemService.addComment(user1.getId(), 456L, commentDto));
    }

    private void createTestObjects() {
        userRequestDto1 = new UserRequestDto();
        userRequestDto1.setName("Michael Kors");
        userRequestDto1.setEmail("kors@fashion.com");

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

        item3 = Item.builder()
                .id(3L)
                .name("Camera")
                .description("Professional DSLR camcorder")
                .available(true)
                .build();

        commentDto = new CommentRequestDto();
        commentDto.setText("This is a comment");
    }
}
