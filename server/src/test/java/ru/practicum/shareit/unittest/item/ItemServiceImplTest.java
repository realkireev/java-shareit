package ru.practicum.shareit.unittest.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
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

@SpringBootTest(classes = ItemServiceImplTest.class)
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

    private User user;
    private Item item1;
    private Item updatedItem1;
    private Item item2;


    @BeforeEach
    public void preparation() {
        user = User.builder()
                .id(1L)
                .name("Rob Williams")
                .build();

        item1 = Item.builder()
                .id(1L)
                .name("Book")
                .owner(user)
                .build();

        item2 = Item.builder()
                .id(2L)
                .name("Collection of books")
                .owner(user)
                .build();

        updatedItem1 = Item.builder()
                .id(1L)
                .name("Updated book")
                .owner(user)
                .build();

        when(mockUserService.findUserById(user.getId())).thenReturn(user);
        when(mockItemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        when(mockItemRepository.save(item1)).thenReturn(item1);
    }

    @Test
    public void shouldCreateItem() {
        ItemResponseDto result = itemService.create(item1, user.getId());

        assertEquals(item1.getId(), result.getId());
        assertEquals(item1.getName(), result.getName());

        verify(mockUserService, times(1)).findUserById(user.getId());
        verify(mockItemRepository, times(1)).save(item1);
    }

    @Test
    public void shouldUpdateItem() {
        ItemResponseDto result = itemService.update(updatedItem1, item1.getId(), user.getId());

        assertEquals(updatedItem1.getId(), result.getId());
        assertEquals(updatedItem1.getName(), result.getName());

        verify(mockItemRepository, times(1)).findById(item1.getId());
        verify(mockItemRepository, times(1)).save(item1);
    }

    @Test
    public void shouldFindItemById() {
        Item result = itemService.findById(item1.getId());

        assertEquals(item1, result);
        verify(mockItemRepository, times(1)).findById(item1.getId());
    }

    @Test
    public void shouldFindItemByOwnerId() {
        List<Item> items = Arrays.asList(item1, item2);
        when(mockItemRepository.findByOwnerId(user.getId())).thenReturn(items);

        List<ItemWithBookingsDto> result = itemService.findByOwnerId(user.getId());
        assertEquals(items.size(), result.size());

        verify(mockItemRepository, times(1)).findByOwnerId(user.getId());
        verify(mockBookingService, times(2)).findLastBookingByItemId(anyLong());
        verify(mockBookingService, times(2)).findNextBookingByItemId(anyLong());
    }

    @Test
    public void shouldSearchItems() {
        String searchText = "bOOk";

        List<Item> items = Arrays.asList(item1, item2);
        when(mockItemRepository.searchByNameOrDescriptionIgnoreCaseAndAvailable(searchText.toLowerCase()))
                .thenReturn(items);

        List<ItemResponseDto> result = itemService.search(searchText);
        assertEquals(items.size(), result.size());
        verify(mockItemRepository, times(1))
                .searchByNameOrDescriptionIgnoreCaseAndAvailable(searchText.toLowerCase());
    }

    @Test
    public void shouldDeleteItem() {
        itemService.delete(item1.getId(), user.getId());

        verify(mockItemRepository, times(1)).findById(item1.getId());
        verify(mockItemRepository, times(1)).delete(item1);
    }

    @Test
    public void shouldFindItemByIdWithBookings() {
        ItemWithBookingsDto result = itemService.findByIdWithBookings(item1.getId(), user.getId());

        assertNotNull(result);
        assertEquals(item1.getId(), result.getId());
        assertEquals(item1.getName(), result.getName());

        verify(mockItemRepository, times(1)).findById(item1.getId());
    }

    @Test
    public void shouldAddCommentToItem() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Great item!");

        when(mockBookingService.hasUserBookedItem(user.getId(), item1.getId())).thenReturn(true);

        Comment savedComment = Comment.builder()
                .id(1L)
                .text("Saved comment")
                .created(LocalDateTime.now().minusDays(1))
                .user(user)
                .build();

        when(mockCommentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponseDto result = itemService.addComment(user.getId(), item1.getId(), commentRequestDto);

        assertNotNull(result);
        assertEquals(savedComment.getId(), result.getId());
        assertEquals(savedComment.getText(), result.getText());
        assertEquals(savedComment.getUser().getName(), result.getAuthorName());
        assertEquals(savedComment.getCreated(), result.getCreated());

        verify(mockUserService, times(1)).findUserById(user.getId());
        verify(mockItemRepository, times(1)).findById(item1.getId());
        verify(mockBookingService, times(1)).hasUserBookedItem(user.getId(), item1.getId());
        verify(mockCommentRepository, times(1)).save(any(Comment.class));
    }
}
