package ru.practicum.shareit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
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
public class RepositoriesTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");
    private static final Pageable PAGEABLE = PageRequest.of(0, 5, SORT_BY_START_DESC);

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Item item3;
    private Request request1;
    private Request request2;
    private Request request3;
    private Booking bookingPast;
    private Booking bookingCurrent;
    private Booking bookingFuture;

    @Test
    public void testFindItemsByOwnerId() {
        createTestObjects();
        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));

        List<Item> foundItems = itemRepository.findByOwnerId(user1.getId());

        assertEquals(2, foundItems.size());
        assertTrue(foundItems.contains(item1));
        assertFalse(foundItems.contains(item2));
        assertTrue(foundItems.contains(item3));
    }

    @Test
    public void testSearchItemsByNameOrDescriptionIgnoreCaseAndAvailable() {
        createTestObjects();
        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));

        List<Item> foundItems = itemRepository.searchByNameOrDescriptionIgnoreCaseAndAvailable("powerful");

        assertEquals(1, foundItems.size());
        assertTrue(foundItems.contains(item1));
        assertFalse(foundItems.contains(item2));
        assertFalse(foundItems.contains(item3));
    }

    @Test
    public void testSaveItemBoundWithRequest() {
        createTestObjects();
        userRepository.saveAll(List.of(user1, user2));
        Item savedItem = itemRepository.save(item1);
        Request savedRequest = requestRepository.save(request1);

        itemRepository.saveItemBoundWithRequest(savedItem.getId(), savedRequest.getId());
        List<Item> itemsBoundToRequest = requestRepository.findItemsByRequestId(request1.getId());

        assertNotNull(itemsBoundToRequest);
        assertEquals(savedItem, itemsBoundToRequest.get(0));
    }

    @Test
    public void testFindUsersByEmailContainingIgnoreCase() {
        User user1 = User.builder()
                .name("Jack")
                .email("jackie@mail.ya")
                .build();

        User user2 = User.builder()
                .name("Daniel")
                .email("daniels@mail.ru")
                .build();

        User user3 = User.builder()
                .name("Winston")
                .email("churchill@mail.ya")
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));

        List<User> foundUsers;
        foundUsers = userRepository.findByEmailContainingIgnoreCase("mAIl");

        assertEquals(3, foundUsers.size());
        assertTrue(foundUsers.contains(user1));
        assertTrue(foundUsers.contains(user2));
        assertTrue(foundUsers.contains(user3));

        foundUsers = userRepository.findByEmailContainingIgnoreCase("Ru");

        assertEquals(1, foundUsers.size());
        assertTrue(foundUsers.contains(user2));
    }

    @Test
    public void testFindBookingByBookerId() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByBookerId(1L, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBookerId());
        assertEquals(2, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndStatusOrderByStartDesc() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(2L,
                BookingStatus.APPROVED, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndEndIsBefore() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByBookerIdAndEndIsBefore(1L,
                LocalDateTime.now().plusSeconds(4), PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBookerId());
        assertEquals(2, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndStartIsAfter() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByBookerIdAndStartIsAfter(2L,
                LocalDateTime.now().minusSeconds(60), PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(3, result.get(0).getItemId());
        assertEquals(2, result.get(1).getBookerId());
        assertEquals(1, result.get(1).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndStartIsBeforeAndEndIsAfter() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                2L,
                LocalDateTime.now().plusSeconds(1),
                LocalDateTime.now().plusSeconds(1),
                PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndItemIdAndEndIsBeforeAndStatus() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatus(2L, 1L,
                LocalDateTime.now().plusDays(1), BookingStatus.APPROVED);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerId() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByOwnerId(user1, PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(3, result.get(0).getItemId());
        assertEquals(2, result.get(1).getBookerId());
        assertEquals(1, result.get(1).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerIdAndStatus() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByOwnerIdAndStatus(user1, BookingStatus.APPROVED, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerIdInFuture() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        List<Booking> result = bookingRepository.findByOwnerIdInFuture(user1, PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(3, result.get(0).getItemId());
        assertEquals(2, result.get(1).getBookerId());
        assertEquals(1, result.get(1).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerIdInPast() throws InterruptedException {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        Thread.sleep(2000);
        List<Booking> result = bookingRepository.findByOwnerIdInPast(user2, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBookerId());
        assertEquals(2, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerIdInCurrent() throws InterruptedException {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        Thread.sleep(2000);
        List<Booking> result = bookingRepository.findByOwnerIdInCurrent(user1, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindLastBookingByItemId() throws InterruptedException {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));

        bookingPast.setStatus(BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        Thread.sleep(2000);
        Booking result = bookingRepository.findLastBookingByItemId(2L).orElse(null);

        assertNotNull(result);
        assertEquals(2, result.getItemId());
        assertEquals(1, result.getBookerId());
    }

    @Test
    public void testFindNextBookingByItemId() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));

        bookingFuture.setStatus(BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        Booking result = bookingRepository.findNextBookingByItemId(3L).orElse(null);

        assertNotNull(result);
        assertEquals(3, result.getItemId());
        assertEquals(2, result.getBookerId());
    }

    @Test
    public void testFindAllRequestsExceptUserId() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        requestRepository.saveAll(List.of(request1, request2, request3));

         List<Request> result = requestRepository.findAllByUserIdNot(2L, PageRequest.of(0, 10)).getContent();

         assertNotNull(result);
         assertEquals(1, result.size());
         assertEquals(1, result.get(0).getUserId());
         assertEquals(request1.getDescription(), result.get(0).getDescription());
    }

    @Test
    public void testFindRequestsByUserId() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        requestRepository.saveAll(List.of(request1, request2, request3));

        List<Request> result = requestRepository.findByUserId(2L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getUserId());
        assertEquals(request2.getDescription(), result.get(0).getDescription());
        assertEquals(2, result.get(1).getUserId());
        assertEquals(request3.getDescription(), result.get(1).getDescription());
    }

    @Test
    public void testFindItemsByItemRequestId() {
        createTestObjects();

        userRepository.saveAll(List.of(user1, user2));
        requestRepository.saveAll(List.of(request1, request2, request3));
        itemRepository.saveAll(List.of(item1, item2, item3));
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

    private void createTestObjects() {
        user1 = User.builder()
                .id(1L)
                .name("Arnold")
                .email("arn@yahoo.com")
                .build();

        user2 = User.builder()
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

        bookingPast = Booking.builder()
                .bookerId(1L)
                .itemId(2L)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .status(BookingStatus.WAITING)
                .build();

        bookingCurrent = Booking.builder()
                .bookerId(2L)
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build();

        bookingFuture = Booking.builder()
                .bookerId(2L)
                .itemId(3L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
    }
}
