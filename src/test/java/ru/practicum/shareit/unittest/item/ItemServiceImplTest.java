package ru.practicum.shareit.unittest.item;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ItemServiceImplTest {
    @Mock
    private UserService mockUserService;

    @Mock
    private ItemRepository mockItemRepository;

    @Mock
    private CommentRepository mockCommentRepository;

    @Mock
    private BookingServiceImpl mockBookingService;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    public void create_WhenValidItemAndOwnerId_ReturnsItemResponseDto() {
        Long ownerId = 1L;

        Item item = Item.builder()
                .id(1L)
                .name("Book")
                .build();

        User owner = User.builder()
                .id(ownerId)
                .build();

        when(mockUserService.findUserById(ownerId)).thenReturn(owner);
        when(mockItemRepository.save(item)).thenReturn(item);

        ItemResponseDto result = itemService.create(item, ownerId);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());

        verify(mockUserService, times(1)).findUserById(ownerId);
        verify(mockItemRepository, times(1)).save(item);
    }

    @Test
    public void update_WhenValidItemAndItemIdAndOwnerId_ReturnsItemResponseDto() {
        Long itemId = 1L;
        Long ownerId = 1L;

        User owner = User.builder()
                .id(ownerId)
                .build();

        Item item = Item.builder()
                .id(itemId)
                .name("Updated Book")
                .build();

        Item storedItem = Item.builder()
                .id(itemId)
                .name("Existing Book")
                .owner(owner)
                .build();

        when(mockItemRepository.findById(itemId)).thenReturn(Optional.of(storedItem));
        when(mockItemRepository.save(storedItem)).thenReturn(storedItem);

        ItemResponseDto result = itemService.update(item, itemId, ownerId);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());

        verify(mockItemRepository, times(1)).findById(itemId);
        verify(mockItemRepository, times(1)).save(storedItem);
    }

    @Test
    public void findById_WhenValidItemId_ReturnsItem() {
        Long itemId = 1L;

        Item item = Item.builder()
                .id(1L)
                .name("Book")
                .build();

        when(mockItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Item result = itemService.findById(itemId);

        assertEquals(item, result);
        verify(mockItemRepository, times(1)).findById(itemId);
    }

    @Test
    public void findByOwnerId_WhenValidOwnerId_ReturnsListOfItemWithBookingsDto() {
        Long ownerId = 1L;

        Item item1 = Item.builder()
                .id(1L)
                .name("Book 1")
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Book 2")
                .build();

        List<Item> items = Arrays.asList(item1, item2);
        when(mockItemRepository.findByOwnerId(ownerId)).thenReturn(items);

        List<ItemWithBookingsDto> result = itemService.findByOwnerId(ownerId);
        assertEquals(items.size(), result.size());

        verify(mockItemRepository, times(1)).findByOwnerId(ownerId);
        verify(mockBookingService, times(2)).findLastBookingByItemId(anyLong());
        verify(mockBookingService, times(2)).findNextBookingByItemId(anyLong());
    }

    @Test
    public void search_WhenValidSearchText_ReturnsListOfItemResponseDto() {
        String searchText = "book";

        Item item1 = Item.builder()
                .id(1L)
                .name("Book 1")
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Book 2")
                .build();

        List<Item> items = Arrays.asList(item1, item2);
        when(mockItemRepository.searchByNameOrDescriptionIgnoreCaseAndAvailable(searchText.toLowerCase())).thenReturn(items);

        List<ItemResponseDto> result = itemService.search(searchText);
        assertEquals(items.size(), result.size());
        verify(mockItemRepository, times(1)).searchByNameOrDescriptionIgnoreCaseAndAvailable(searchText.toLowerCase());
    }

    @Test
    public void delete_WhenValidItemIdAndOwnerId_DeletesItemFromRepository() {
        Long itemId = 1L;
        Long ownerId = 1L;

        User owner = User.builder()
                .id(ownerId)
                .build();

        Item storedItem = Item.builder()
                .id(itemId)
                .name("Existing Book")
                .owner(owner)
                .build();

        when(mockItemRepository.findById(itemId)).thenReturn(Optional.of(storedItem));

        itemService.delete(itemId, ownerId);

        verify(mockItemRepository, times(1)).findById(itemId);
        verify(mockItemRepository, times(1)).delete(storedItem);
    }

    @Test
    public void findByIdWithBookings_WhenValidItemIdAndUserId_ReturnsItemWithBookingsDto() {
        Long itemId = 1L;
        Long userId = 1L;

        Item item = Item.builder()
                .id(itemId)
                .name("Book")
                .build();

        User user = User.builder()
                .id(userId)
                .name("Lars Ulrich")
                .build();

        item.setOwner(user);

        when(mockItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemWithBookingsDto result = itemService.findByIdWithBookings(itemId, userId);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());

        verify(mockItemRepository, times(1)).findById(itemId);
    }

    @Test
    public void addComment_WhenValidUserIdAndItemIdAndCommentRequestDto_ReturnsCommentResponseDto() {
        Long userId = 1L;
        Long itemId = 1L;

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Great item!");

        User user = User.builder()
                .id(userId)
                .name("Rob Williams")
                .build();

        Item item = Item.builder()
                .id(itemId)
                .build();

        when(mockUserService.findUserById(userId)).thenReturn(user);
        when(mockItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(mockBookingService.hasUserBookedItem(userId, itemId)).thenReturn(true);

        Comment savedComment = Comment.builder()
                .id(1L)
                .text("Saved comment")
                .created(LocalDateTime.now().minusDays(1))
                .user(user)
                .build();

        when(mockCommentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponseDto result = itemService.addComment(userId, itemId, commentRequestDto);

        assertNotNull(result);
        assertEquals(savedComment.getId(), result.getId());
        assertEquals(savedComment.getText(), result.getText());
        assertEquals(savedComment.getUser().getName(), result.getAuthorName());
        assertEquals(savedComment.getCreated(), result.getCreated());

        verify(mockUserService, times(1)).findUserById(userId);
        verify(mockItemRepository, times(1)).findById(itemId);
        verify(mockBookingService, times(1)).hasUserBookedItem(userId, itemId);
        verify(mockCommentRepository, times(1)).save(any(Comment.class));
    }
}
