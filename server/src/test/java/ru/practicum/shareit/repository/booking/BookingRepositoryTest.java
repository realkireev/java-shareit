package ru.practicum.shareit.repository.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.practicum.shareit.common.Variables.SORT_BY_START_DESC;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private static final Pageable PAGEABLE = PageRequest.of(0, 5, SORT_BY_START_DESC);
    private User user1;
    private User user2;
    private Booking bookingPast;
    private Booking bookingCurrent;
    private Booking bookingFuture;

    @BeforeEach
    public void preparation() {
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

        Item item1 = Item.builder()
                .name("Laptop")
                .description("Powerful laptop")
                .available(true)
                .owner(user1)
                .build();

        Item item2 = Item.builder()
                .name("Phone")
                .description("Smartphone")
                .available(true)
                .owner(user2)
                .build();

        Item item3 = Item.builder()
                .name("Tablet")
                .description("Portable tablet")
                .available(true)
                .owner(user1)
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

        userRepository.saveAll(List.of(user1, user2));
        itemRepository.saveAll(List.of(item1, item2, item3));
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

    }

    @Test
    public void testFindBookingByBookerId() {
        List<Booking> result = bookingRepository.findByBookerId(1L, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBookerId());
        assertEquals(2, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndStatusOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(2L,
                BookingStatus.APPROVED, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndEndIsBefore() {
        List<Booking> result = bookingRepository.findByBookerIdAndEndIsBefore(1L,
                LocalDateTime.now().plusSeconds(4), PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBookerId());
        assertEquals(2, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByBookerIdAndStartIsAfter() {
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
        List<Booking> result = bookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatusOrderById(2L, 1L,
                LocalDateTime.now().plusDays(1), BookingStatus.APPROVED);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerId() {
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
        List<Booking> result = bookingRepository.findByOwnerIdAndStatus(user1, BookingStatus.APPROVED, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerIdInFuture() {
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
        Thread.sleep(2000);
        List<Booking> result = bookingRepository.findByOwnerIdInPast(user2, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBookerId());
        assertEquals(2, result.get(0).getItemId());
    }

    @Test
    public void testFindBookingsByOwnerIdInCurrent() throws InterruptedException {
        Thread.sleep(2000);
        List<Booking> result = bookingRepository.findByOwnerIdInCurrent(user1, PAGEABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBookerId());
        assertEquals(1, result.get(0).getItemId());
    }

    @Test
    public void testFindLastBookingByItemId() throws InterruptedException {
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
        bookingFuture.setStatus(BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(bookingPast, bookingCurrent, bookingFuture));

        Booking result = bookingRepository.findNextBookingByItemId(3L).orElse(null);

        assertNotNull(result);
        assertEquals(3, result.getItemId());
        assertEquals(2, result.getBookerId());
    }
}
