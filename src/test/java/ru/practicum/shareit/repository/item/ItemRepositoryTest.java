package ru.practicum.shareit.repository.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    private User user1;
    private Item item1;
    private Item item2;
    private Item item3;
    private Request request1;
    private Request request2;
    private Request request3;

    @BeforeEach
    public void preparation() {
        user1 = User.builder()
                .id(1L)
                .name("Arnold")
                .email("arn@yahoo.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Bobbie")
                .email("bob@yahoo.com")
                .build();

        item1 = Item.builder()
                .name("Laptop")
                .description("Powerful laptop")
                .available(true)
                .owner(user1)
                .build();

        item2 = Item.builder()
                .name("Phone")
                .description("Smartphone")
                .available(true)
                .owner(user2)
                .build();

        item3 = Item.builder()
                .name("Tablet")
                .description("Portable tablet")
                .available(true)
                .owner(user1)
                .build();

        request1 = Request.builder()
                .description("I need a tablet!")
                .userId(1L)
                .created(LocalDateTime.now())
                .build();

        request2 = Request.builder()
                .description("Seeking for a microphone :)")
                .userId(2L)
                .created(LocalDateTime.now())
                .build();

        request3 = Request.builder()
                .description("A wedding suit for one night")
                .userId(2L)
                .created(LocalDateTime.now())
                .build();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
    }

    @Test
    public void testFindItemsByOwnerId() {
        List<Item> foundItems = itemRepository.findByOwnerId(user1.getId());

        assertEquals(2, foundItems.size());
        assertTrue(foundItems.contains(item1));
        assertFalse(foundItems.contains(item2));
        assertTrue(foundItems.contains(item3));
    }

    @Test
    public void testSearchItemsByNameOrDescriptionIgnoreCaseAndAvailable() {
        List<Item> foundItems = itemRepository.searchByNameOrDescriptionIgnoreCaseAndAvailable("powerful");

        assertEquals(1, foundItems.size());
        assertTrue(foundItems.contains(item1));
        assertFalse(foundItems.contains(item2));
        assertFalse(foundItems.contains(item3));
    }

    @Test
    public void testSaveItemBoundWithRequest() {
        Item savedItem = itemRepository.save(item1);
        Request savedRequest = requestRepository.save(request1);

        itemRepository.saveItemBoundWithRequest(savedItem.getId(), savedRequest.getId());
        List<Item> itemsBoundToRequest = requestRepository.findItemsByRequestId(request1.getId());

        assertNotNull(itemsBoundToRequest);
        assertEquals(savedItem, itemsBoundToRequest.get(0));
    }

    @Test
    public void testFindItemsByItemRequestId() {
        requestRepository.saveAll(List.of(request1, request2, request3));
        itemRepository.saveItemBoundWithRequest(2L, 2L);
        itemRepository.saveItemBoundWithRequest(3L, 2L);

        List<Item> result = requestRepository.findItemsByRequestId(2L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getId());
        assertEquals(item2.getName(), result.get(0).getName());
        assertEquals(item2.getDescription(), result.get(0).getDescription());

        assertEquals(3, result.get(1).getId());
        assertEquals(item3.getName(), result.get(1).getName());
        assertEquals(item3.getDescription(), result.get(1).getDescription());
    }
}
