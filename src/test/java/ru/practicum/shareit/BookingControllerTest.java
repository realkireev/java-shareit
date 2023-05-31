package ru.practicum.shareit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static final String ENDPOINT = "/bookings";
    private static final String CONTENT_TYPE = "application/json";
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final List<User> users = createUserObjects();

    private final Item item1 = Item.builder()
            .id(1L)
            .name("Дрель")
            .description("Электрическая дрель")
            .owner(users.get(0))
            .available(true)
            .build();

    private final Item item2 = Item.builder()
            .id(2L)
            .name("Отвертка")
            .description("Двуручная отвертка")
            .owner(users.get(3))
            .available(false)
            .build();

    private final Item item3 = Item.builder()
            .id(3L)
            .name("Самосвал")
            .description("Трехколесный")
            .owner(users.get(3))
            .available(true)
            .build();

    private final Item item4 = Item.builder()
            .id(4L)
            .name("Граммофон")
            .description("С новыми иголками!")
            .owner(users.get(5))
            .available(true)
            .build();

    private final Booking booking1 = Booking.builder()
            .id(1L)
            .itemId(item2.getId())
            .bookerId(users.get(0).getId())
            .start(LocalDateTime.now().plusSeconds(3))
            .end(LocalDateTime.now().plusSeconds(4))
            .status(BookingStatus.WAITING)
            .build();

    private final Booking booking2 = Booking.builder()
            .id(2L)
            .itemId(item2.getId())
            .bookerId(users.get(0).getId())
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .status(BookingStatus.WAITING)
            .build();

    private final Booking booking3 = Booking.builder()
            .id(3L)
            .itemId(item1.getId())
            .bookerId(users.get(3).getId())
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(1).plusHours(1))
            .status(BookingStatus.WAITING)
            .build();

    private final Booking booking4 = Booking.builder()
            .id(4L)
            .itemId(item2.getId())
            .bookerId(users.get(4).getId())
            .start(LocalDateTime.now().plusHours(1))
            .end(LocalDateTime.now().plusHours(2))
            .status(BookingStatus.WAITING)
            .build();

    private final Booking booking5 = Booking.builder()
            .id(5L)
            .itemId(item3.getId())
            .bookerId(users.get(0).getId())
            .start(LocalDateTime.now().plusSeconds(3))
            .end(LocalDateTime.now().plusDays(1))
            .status(BookingStatus.WAITING)
            .build();

    private final Booking booking6 = Booking.builder()
            .id(6L)
            .itemId(item2.getId())
            .bookerId(users.get(0).getId())
            .start(LocalDateTime.now().plusSeconds(3))
            .end(LocalDateTime.now().plusSeconds(5))
            .status(BookingStatus.WAITING)
            .build();

    private final Booking booking7 = Booking.builder()
            .id(7L)
            .itemId(item1.getId())
            .bookerId(users.get(4).getId())
            .start(LocalDateTime.now().plusDays(10))
            .end(LocalDateTime.now().plusDays(11))
            .status(BookingStatus.WAITING)
            .build();

    private final Booking booking8 = Booking.builder()
            .id(8L)
            .itemId(item4.getId())
            .bookerId(users.get(0).getId())
            .start(LocalDateTime.now().plusSeconds(2))
            .end(LocalDateTime.now().plusHours(1))
            .status(BookingStatus.WAITING)
            .build();

    @Test
    @Order(1)
    public void shouldReturnBadRequestWithBookingOfUnavailableItem() throws Exception {
        sendRequestsToCreateEntities();

        Long itemId = item2.getId();
        Long userId = users.get(0).getId();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());

        item2.setAvailable(true);
        updateItemInDatabase(item2);
    }

    @Test
    @Order(2)
    public void shouldReturnNotFoundWithUnknownUser() throws Exception {
        long itemId = item2.getId();
        long userId = 100;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    public void shouldReturnNotFoundWithUnknownItem() throws Exception {
        long itemId = 200;
        long userId = users.get(0).getId();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    public void shouldReturnBadRequestWithEndInPast() throws Exception {
        long itemId = item2.getId();
        long userId = users.get(0).getId();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void shouldReturnBadRequestWithEndBeforeStart() throws Exception {
        long itemId = item2.getId();
        long userId = users.get(0).getId();
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void shouldReturnBadRequestWithStartEqualsEnd() throws Exception {
        long itemId = item2.getId();
        long userId = users.get(0).getId();
        LocalDateTime start = LocalDateTime.now().plusDays(2);

        String body = createJsonBooking(itemId, start, start);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void shouldReturnBadRequestWithStartEqualsNull() throws Exception {
        long itemId = item2.getId();
        long userId = users.get(0).getId();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, null, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Order(8)
    @Test
    public void shouldReturnBadRequestWithEndEqualsNull() throws Exception {
        long itemId = item2.getId();
        long userId = users.get(0).getId();
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, start, null);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    public void shouldReturnBadRequestWithStartInPast() throws Exception {
        long itemId = item2.getId();
        long userId = users.get(0).getId();
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    public void shouldCreateBooking1User1Item2() throws Exception {
        String body = createJsonBooking(booking1.getItemId(), booking1.getStart(), booking1.getEnd());

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, booking1.getBookerId())
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking1.getId()))
                .andExpect(jsonPath("$.start").value(booking1.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking1.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(11)
    public void shouldApproveBookingByOwnerUser4() throws Exception {
        long ownerId = 4;

        mockMvc.perform(patch(ENDPOINT + "/" + booking1.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking1.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(12)
    public void shouldCreateBooking2User1Item2() throws Exception {
        String body = createJsonBooking(booking2.getItemId(), booking2.getStart(), booking2.getEnd());
        long bookerId = 1;

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, bookerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking2.getId()))
                .andExpect(jsonPath("$.start").value(booking2.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking2.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(13)
    public void shouldReturnBooking2ByUser1Booker() throws Exception {
        long expectedId = 2;
        long bookerId = 1;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId)
            .header(HEADER_USER_ID, bookerId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(booking2.getId()))
            .andExpect(jsonPath("$.status").value(booking2.getStatus().toString()))
            .andExpect(jsonPath("$.booker.id").value(booking2.getBookerId()))
            .andExpect(jsonPath("$.item.id").value(booking2.getItemId()))
            .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(14)
    public void shouldReturnBooking2ByUser4Owner() throws Exception {
        long expectedId = 2;
        long ownerId = 4;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId)
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking2.getId()))
                .andExpect(jsonPath("$.status").value(booking2.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(booking2.getItemId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(15)
    public void shouldReturnNotFoundIfGetAllBookingsByUnknownUser() throws Exception {
        long userId = 100;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(16)
    public void shouldReturnNotFoundIfGetAllBookingsByUnknownOwner() throws Exception {
        long ownerId = 100;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(17)
    public void shouldReturnAllBookingsForUser1() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(booking2.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking2.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking1.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item2.getName()));
    }


    @Test
    @Order(18)
    public void shouldReturnAllBookingsForUser1ByALLState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(booking2.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking2.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking1.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item2.getName()));
    }

    @Test
    @Order(19)
    public void shouldReturnAllBookingsForUser1ByFUTUREState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "FUTURE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(booking2.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking2.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking1.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item2.getName()));
    }

    @Test
    @Order(20)
    public void shouldReturnBadRequestForUser1ByWrongState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "UNSUPPORTED_STATUS"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(21)
    public void shouldReturnAllBookingsForOwner() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(booking2.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking2.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking1.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item2.getName()));
    }

    @Test
    @Order(22)
    public void shouldReturnAllBookingsForOwnerByALLState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("state", "ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(booking2.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking2.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking1.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item2.getName()));
    }

    @Test
    @Order(23)
    public void shouldReturnAllBookingsForOwnerByFutureState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("state", "FUTURE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(booking2.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking2.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking1.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item2.getName()));
    }

    @Test
    @Order(24)
    public void shouldReturnBadRequestForOwnerByWrongState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("state", "UNSUPPORTED_STATE"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(25)
    public void shouldReturnNotFoundForWrongBookingId() throws Exception {
        long bookingId = 1000;
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/" + bookingId)
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(26)
    public void shouldReturnNotFoundBooking1AndUser5() throws Exception {
        long bookingId = 1;
        long userId = 5;

        mockMvc.perform(get(ENDPOINT + "/" + bookingId)
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(27)
    public void shouldReturnNotFoundIfApproveNotByOwner() throws Exception {
        long userId = 5;

        mockMvc.perform(patch(ENDPOINT + "/" + booking2.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(28)
    public void shouldReturnNotFoundIfApproveByBooker() throws Exception {
        long userId = 1;

        mockMvc.perform(patch(ENDPOINT + "/" + booking2.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(29)
    public void shouldApproveBooking2ByOwnerUser4() throws Exception {
        long userId = 4;

        mockMvc.perform(patch(ENDPOINT + "/" + booking2.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking2.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(booking2.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(30)
    public void shouldReturnBadRequestIfApproveBooking2AlreadyApproved() throws Exception {
        long userId = 4;

        mockMvc.perform(patch(ENDPOINT + "/" + booking2.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(31)
    public void shouldReturnNotFoundIfOwnerIsBooker() throws Exception {
        long itemId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(32)
    public void shouldCreateBooking3User4Item1() throws Exception {
        String body = createJsonBooking(booking3.getItemId(), booking3.getStart(), booking3.getEnd());

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, booking3.getBookerId())
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking3.getId()))
                .andExpect(jsonPath("$.start").value(booking3.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking3.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(booking3.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item1.getId()))
                .andExpect(jsonPath("$.item.name").value(item1.getName()));
    }

    @Test
    @Order(33)
    public void shouldRejectBooking3ByOwnerUser1() throws Exception {
        long ownerId = 1;
        mockMvc.perform(patch(ENDPOINT + "/" + booking3.getId())
                        .queryParam("approved", "false")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking3.getId()))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.booker.id").value(booking3.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item1.getId()))
                .andExpect(jsonPath("$.item.name").value(item1.getName()));
    }

    @Test
    @Order(34)
    public void shouldCreateBooking4User5Item2() throws Exception {
        String body = createJsonBooking(booking4.getItemId(), booking4.getStart(), booking4.getEnd());
        long bookerId = 5;

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, bookerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking4.getId()))
                .andExpect(jsonPath("$.start").value(booking4.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking4.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(booking4.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(35)
    public void shouldApproveBooking4ByOwnerUser4() throws Exception {
        long ownerId = 4;

        mockMvc.perform(patch(ENDPOINT + "/" + booking4.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking4.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(booking4.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(36)
    public void shouldAReturnItem2GetByUser4WithBookings() throws Exception {
        long ownerId = 4;

        mockMvc.perform(get("/items/" + item2.getId())
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item2.getId()))
                .andExpect(jsonPath("$.name").value(item2.getName()))
                .andExpect(jsonPath("$.description").value(item2.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.lastBooking").value(nullValue()))
                .andExpect(jsonPath("$.nextBooking.id").value(1))
                .andExpect(jsonPath("$.nextBooking.bookerId").value(1));
    }

    @Test
    @Order(37)
    public void shouldAReturnItem2GetByUser1WithoutBookings() throws Exception {
        long userId = 1;

        mockMvc.perform(get("/items/" + item2.getId())
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item2.getId()))
                .andExpect(jsonPath("$.name").value(item2.getName()))
                .andExpect(jsonPath("$.description").value(item2.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.lastBooking").value(nullValue()))
                .andExpect(jsonPath("$.nextBooking").value(nullValue()));
    }

    @Test
    @Order(38)
    public void shouldAReturnItem2GetByUser5WithoutBookings() throws Exception {
        long userId = 5;

        mockMvc.perform(get("/items/" + item2.getId())
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item2.getId()))
                .andExpect(jsonPath("$.name").value(item2.getName()))
                .andExpect(jsonPath("$.description").value(item2.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.lastBooking").value(nullValue()))
                .andExpect(jsonPath("$.nextBooking").value(nullValue()));
    }

    @Test
    @Order(39)
    public void shouldAReturnAllItemsGetByUser4WithBookings() throws Exception {
        long userId = 4;

        mockMvc.perform(get("/items")
                        .header(HEADER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(item2.getId()))
                .andExpect(jsonPath("$[0].name").value(item2.getName()))
                .andExpect(jsonPath("$[0].description").value(item2.getDescription()))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].lastBooking").value(nullValue()))
                .andExpect(jsonPath("$[0].nextBooking.id").value(1))
                .andExpect(jsonPath("$[0].nextBooking.bookerId").value(1));
    }

    @Test
    @Order(40)
    public void shouldCreateBooking5User1Item3() throws Exception {
        String body = createJsonBooking(booking5.getItemId(), booking5.getStart(), booking5.getEnd());
        long bookerId = 1;

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, bookerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking5.getId()))
                .andExpect(jsonPath("$.start").value(booking5.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking5.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item3.getId()))
                .andExpect(jsonPath("$.item.name").value(item3.getName()));
    }

    @Test
    @Order(41)
    public void shouldReturnAllBookingsForUser1ByWAITINGState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "WAITING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(booking5.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking5.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item3.getName()));
    }

    @Test
    @Order(42)
    public void shouldReturnAllBookingsForUser4OwnerByWaitingState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("state", "WAITING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(booking5.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking5.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item3.getName()));
    }

    @Test
    @Order(43)
    public void shouldCreateBooking6User1Item2() throws Exception {
        String body = createJsonBooking(booking6.getItemId(), booking6.getStart(), booking6.getEnd());

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, booking1.getBookerId())
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking6.getId()))
                .andExpect(jsonPath("$.start").value(booking6.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking6.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(booking6.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(44)
    public void shouldRejectBooking5ByOwnerUser4() throws Exception {
        long ownerId = 4;

        mockMvc.perform(patch(ENDPOINT + "/" + booking5.getId())
                        .queryParam("approved", "false")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking5.getId()))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item3.getId()))
                .andExpect(jsonPath("$.item.name").value(item3.getName()));
    }

    @Test
    @Order(45)
    public void shouldReturnAllBookingsForUser1ByREJECTEDState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "REJECTED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(booking5.getId()))
                .andExpect(jsonPath("$[0].status").value("REJECTED"))
                .andExpect(jsonPath("$[0].booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking5.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item3.getName()));
    }

    @Test
    @Order(46)
    public void shouldReturnAllBookingsForUser4OwnerByREJECTEDState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("state", "REJECTED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(booking5.getId()))
                .andExpect(jsonPath("$[0].status").value("REJECTED"))
                .andExpect(jsonPath("$[0].booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking5.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item3.getName()));
    }

    @Test
    @Order(47)
    public void shouldApproveBooking6ByOwnerUser4() throws Exception {
        long ownerId = 4;

        mockMvc.perform(patch(ENDPOINT + "/" + booking6.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking6.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(booking6.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item2.getId()))
                .andExpect(jsonPath("$.item.name").value(item2.getName()));
    }

    @Test
    @Order(48)
    public void shouldReturnItem1ByUser1() throws Exception {
        long ownerId = 1;

        mockMvc.perform(get("/items/1").header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item1.getId()))
                .andExpect(jsonPath("$.name").value(item1.getName()))
                .andExpect(jsonPath("$.description").value(item1.getDescription()))
                .andExpect(jsonPath("$.available").value(item1.getAvailable()))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @Order(49)
    public void shouldCreateBooking7User5Item1() throws Exception {
        String body = createJsonBooking(booking7.getItemId(), booking7.getStart(), booking7.getEnd());

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, booking7.getBookerId())
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking7.getId()))
                .andExpect(jsonPath("$.start").value(booking7.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking7.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(booking7.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item1.getId()))
                .andExpect(jsonPath("$.item.name").value(item1.getName()));
    }

    @Test
    @Order(50)
    public void shouldCreateBooking8User1Item4() throws Exception {
        String body = createJsonBooking(booking8.getItemId(), booking8.getStart(), booking8.getEnd());

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_USER_ID, booking8.getBookerId())
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking8.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value(booking8.getStart().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(booking8.getEnd().format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.booker.id").value(booking8.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item4.getId()))
                .andExpect(jsonPath("$.item.name").value(item4.getName()));
    }

    @Test
    @Order(51)
    public void shouldApproveBooking8ByOwnerUser6() throws Exception {
        long ownerId = 6;

        mockMvc.perform(patch(ENDPOINT + "/" + booking8.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking8.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(booking8.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item4.getId()))
                .andExpect(jsonPath("$.item.name").value(item4.getName()));
    }

    @Test
    @Order(52)
    public void shouldReturnItem2ByUser1WithoutComments() throws Exception {
        long ownerId = 1;

        mockMvc.perform(get("/items/2").header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item2.getId()))
                .andExpect(jsonPath("$.name").value(item2.getName()))
                .andExpect(jsonPath("$.description").value(item2.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @Order(53)
    public void shouldReturnItem2ByUser4OwnerWithoutComments() throws Exception {
        long ownerId = 4;

        mockMvc.perform(get("/items/2").header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item2.getId()))
                .andExpect(jsonPath("$.name").value(item2.getName()))
                .andExpect(jsonPath("$.description").value(item2.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @Order(54)
    public void shouldReturnBadRequestWhenCommentWithoutBookingUser4ToItem1() throws Exception {
        long userId = 4;
        long itemId = 1;
        String body = "{\"text\": \"Comment for item 1\"}";
        String url = String.format("/items/%d/comment", itemId);

        mockMvc.perform(post(url)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(55)
    public void shouldReturnBadRequestWhenCommentIsEmpty() throws Exception {
        long userId = 1;
        long itemId = 2;
        String body = "{\"text\": \"\"}";
        String url = String.format("/items/%d/comment", itemId);

        mockMvc.perform(post(url)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(56)
    public void shouldCreateCommentByUser1ForItem2() throws Exception {
        Thread.sleep(3500);

        long userId = 1;
        long itemId = 2;
        String body = "{\"text\": \"Very good item!\"}";
        String url = String.format("/items/%d/comment", itemId);

        mockMvc.perform(post(url)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Very good item!"))
                .andExpect(jsonPath("$.authorName").value(users.get(0).getName()))
                .andExpect(jsonPath("$.created").value(notNullValue()));
    }

    @Test
    @Order(57)
    public void shouldReturnItem2ByUser1WithComments() throws Exception {
        long ownerId = 1;

        mockMvc.perform(get("/items/2").header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item2.getId()))
                .andExpect(jsonPath("$.name").value(item2.getName()))
                .andExpect(jsonPath("$.description").value(item2.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id").value(1))
                .andExpect(jsonPath("$.comments[0].text").value("Very good item!"))
                .andExpect(jsonPath("$.comments[0].authorName").value(users.get(0).getName()))
                .andExpect(jsonPath("$.comments[0].created").value(notNullValue()));
    }

    @Test
    @Order(58)
    public void shouldReturnItem2ByUser4OwnerWithComments() throws Exception {
        long ownerId = 4;

        mockMvc.perform(get("/items/2").header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item2.getId()))
                .andExpect(jsonPath("$.name").value(item2.getName()))
                .andExpect(jsonPath("$.description").value(item2.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id").value(1))
                .andExpect(jsonPath("$.comments[0].text").value("Very good item!"))
                .andExpect(jsonPath("$.comments[0].authorName").value(users.get(0).getName()))
                .andExpect(jsonPath("$.comments[0].created").value(notNullValue()));
    }

    @Test
    @Order(59)
    public void shouldApproveBooking7ByOwnerUser1() throws Exception {
        long ownerId = 1;

        mockMvc.perform(patch(ENDPOINT + "/" + booking7.getId())
                        .queryParam("approved", "true")
                        .header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking7.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(booking7.getBookerId()))
                .andExpect(jsonPath("$.item.id").value(item1.getId()))
                .andExpect(jsonPath("$.item.name").value(item1.getName()));
    }

    @Test
    @Order(60)
    public void shouldReturnBadRequestForCommentForFutureBooking() throws Exception {
        long userId = 5;
        long itemId = 1;
        String body = "{\"text\": \"Very good item!\"}";
        String url = String.format("/items/%d/comment", itemId);

        mockMvc.perform(post(url)
                        .header(HEADER_USER_ID, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(61)
    public void shouldReturnAllBookingsForUser1ByCURRENTState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "CURRENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(booking6.getId()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].booker.id").value(booking6.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking6.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking5.getId()))
                .andExpect(jsonPath("$[1].status").value("REJECTED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking5.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item3.getName()))

                .andExpect(jsonPath("$[2].id").value(booking8.getId()))
                .andExpect(jsonPath("$[2].status").value("APPROVED"))
                .andExpect(jsonPath("$[2].booker.id").value(booking8.getBookerId()))
                .andExpect(jsonPath("$[2].item.id").value(booking8.getItemId()))
                .andExpect(jsonPath("$[2].item.name").value(item4.getName()));
    }

    @Test
    @Order(62)
    public void shouldReturnAllBookingsForUser4OwnerByCURRENTState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("state", "CURRENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(booking6.getId()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].booker.id").value(booking6.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking6.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()))

                .andExpect(jsonPath("$[1].id").value(booking5.getId()))
                .andExpect(jsonPath("$[1].status").value("REJECTED"))
                .andExpect(jsonPath("$[1].booker.id").value(booking5.getBookerId()))
                .andExpect(jsonPath("$[1].item.id").value(booking5.getItemId()))
                .andExpect(jsonPath("$[1].item.name").value(item3.getName()));
    }

    @Test
    @Order(63)
    public void shouldReturnAllBookingsForUser1ByPASTState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "PAST"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(booking1.getId()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].booker.id").value(booking1.getBookerId()))
                .andExpect(jsonPath("$[0].item.id").value(booking1.getItemId()))
                .andExpect(jsonPath("$[0].item.name").value(item2.getName()));
    }

    @Test
    @Order(64)
    public void shouldReturnAllBookingsForUser4OwnerByPASTState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("state", "PAST"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(65)
    public void shouldReturnItem6ByUser4OwnerWithoutComments() throws Exception {
        long ownerId = 4;

        mockMvc.perform(get("/items/3").header(HEADER_USER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item3.getId()))
                .andExpect(jsonPath("$.name").value(item3.getName()))
                .andExpect(jsonPath("$.description").value(item3.getDescription()))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @Order(66)
    public void shouldReturnBadRequestOnGetAllWithFrom0Size0() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(67)
    public void shouldReturnBadRequestOnOwnerGetAllWithFrom0Size0() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(68)
    public void shouldReturnBadRequestOnGetAllWithFromNegativeSize0() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("from", "-1")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(69)
    public void shouldReturnBadRequestOnOwnerGetAllWithFromNegativeSize0() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("from", "-1")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(70)
    public void shouldReturnBadRequestOnGetAllWithFrom0SizeNegative() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(71)
    public void shouldReturnBadRequestOnOwnerGetAllWithFrom0SizeNegative() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(72)
    public void shouldReturnBooking8OnGetAllWithFrom4Size2() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_USER_ID, userId)
                        .param("from", "4")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Order(73)
    public void shouldReturnBooking3OnGetAllForOwnerWithFrom1Size1() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(HEADER_USER_ID, userId)
                        .param("from", "1")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(booking3.getId()))
                .andExpect(jsonPath("$[0].item.id").value(item1.getId()))
                .andExpect(jsonPath("$[0].item.name").value(item1.getName()))
                .andExpect(jsonPath("$[0].item.description").value(item1.getDescription()));
    }

    private void sendRequestsToCreateEntities() throws Exception {
        users.forEach(this::sendUserToDatabase);

        sendItemToDatabase(item1);
        sendItemToDatabase(item2);
        sendItemToDatabase(item3);
        sendItemToDatabase(item4);
    }

    private String createJsonBooking(Long itemId, LocalDateTime start, LocalDateTime end) throws JsonProcessingException {
        Map<String, Object> object = createJsonMapBooking(itemId, start, end);
        return objectMapper.writeValueAsString(object);
    }

    private Map<String, Object> createJsonMapBooking(Long itemId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> object = new HashMap<>();
        if (itemId != null) {
            object.put("itemId", itemId);
        }
        if (start != null) {
            object.put("start", start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (end != null) {
            object.put("end", end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return object;
    }

    private void sendUserToDatabase(User user) {
        try {
            String body = objectMapper.writeValueAsString(user);

            mockMvc.perform(post("/users")
                    .contentType(CONTENT_TYPE)
                    .content(body));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendItemToDatabase(Item item) throws Exception {
        String body = objectMapper.writeValueAsString(item);

        mockMvc.perform(post("/items")
                .header(HEADER_USER_ID, item.getOwner().getId())
                .contentType(CONTENT_TYPE)
                .content(body));
    }

    private void updateItemInDatabase(Item item) throws Exception {
        String body = objectMapper.writeValueAsString(item);

        mockMvc.perform(patch("/items/" + item.getId())
                .header(HEADER_USER_ID, item.getOwner().getId())
                .contentType(CONTENT_TYPE)
                .content(body));
    }

    private List<User> createUserObjects() {
        List<User> users = new ArrayList<>();

        for (long i = 1; i <= 6; i++) {
            User user = User.builder()
                    .id(i)
                    .name(String.format("User%d", i))
                    .email(String.format("user%d@mail.com", i))
                    .build();
            users.add(user);
        }

        return users;
    }
}
