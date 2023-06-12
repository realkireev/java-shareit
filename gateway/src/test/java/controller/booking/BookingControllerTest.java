package controller.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.booking.cacheservice.BookingCacheService;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.Variables.CONTENT_TYPE;
import static ru.practicum.shareit.common.Variables.USER_HEADER;

@SpringBootTest(classes = { ShareItGateway.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private BookingCacheService bookingCacheService;

    @InjectMocks
    private BookingController bookingController;

    private static final String ENDPOINT = "/bookings";
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @Order(1)
    public void testCreateBooking() {
        long itemId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();

        BookingRequestDto bookingRequestDto = getBookingRequestDto(itemId, start, end);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1,
                ItemResponseDto.builder().build(), UserResponseDto.builder().build(), start, end);

        when(bookingCacheService.create(userId, bookingRequestDto)).thenReturn(expectedDto);

        ResponseEntity<Object> result = bookingController.create(bookingRequestDto, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(bookingCacheService, times(1)).create(userId, bookingRequestDto);
    }

    @Test
    @Order(2)
    public void testApproveBooking() {
        long bookingId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();

        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1,
                ItemResponseDto.builder().build(), UserResponseDto.builder().build(), start, end);

        when(bookingCacheService.approve(bookingId, userId, true)).thenReturn(expectedDto);

        ResponseEntity<Object> result = bookingController.approve(true, bookingId, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(bookingCacheService, times(1)).approve(bookingId, userId, true);
    }

    @Test
    @Order(3)
    public void testFindById() {
        long bookingId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();

        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1,
                ItemResponseDto.builder().build(), UserResponseDto.builder().build(), start, end);

        when(bookingCacheService.findById(bookingId, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = bookingController.findById(bookingId, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(bookingCacheService, times(1)).findById(bookingId, userId);
    }

    @Test
    @Order(4)
    public void shouldReturnBadRequestWithEndInPast() throws Exception {
        long itemId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void shouldReturnBadRequestWithEndBeforeStart() throws Exception {
        long itemId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void shouldReturnBadRequestWithStartEqualsEnd() throws Exception {
        long itemId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(2);

        String body = createJsonBooking(itemId, start, start);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void shouldReturnBadRequestWithStartEqualsNull() throws Exception {
        long itemId = 1;
        long userId = 1;
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, null, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Order(8)
    @Test
    public void shouldReturnBadRequestWithEndEqualsNull() throws Exception {
        long itemId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, start, null);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    public void shouldReturnBadRequestWithStartInPast() throws Exception {
        long itemId = 1;
        long userId = 1;
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        String body = createJsonBooking(itemId, start, end);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    public void testFindAllByUserIdAndState() {
        long userId = 1;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();

        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1,
                ItemResponseDto.builder().build(), UserResponseDto.builder().build(), start, end);

        when(bookingCacheService.findAllByUserIdAndState(userId, "ALL", 0, 20)).thenReturn(expectedDto);

        ResponseEntity<Object> result = bookingController.findAllByUserIdAndState(userId, "ALL", 0, 20);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(bookingCacheService, times(1)).findAllByUserIdAndState(userId, "ALL", 0, 20);
    }

    @Test
    @Order(11)
    public void testFindAllByOwnerIdAndState() {
        long userId = 1;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();

        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1,
                ItemResponseDto.builder().build(), UserResponseDto.builder().build(), start, end);

        when(bookingCacheService.findAllByOwnerIdAndState(userId, "ALL", 0, 20)).thenReturn(expectedDto);

        ResponseEntity<Object> result = bookingController.findAllByOwnerIdAndState(userId, "ALL", 0, 20);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(bookingCacheService, times(1)).findAllByOwnerIdAndState(userId, "ALL", 0, 20);
    }

    @Test
    @Order(20)
    public void shouldReturnBadRequestForUser1ByWrongState() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .param("state", "UNSUPPORTED_STATUS"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    public void shouldReturnBadRequestForOwnerByWrongState() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/owner")
                        .header(USER_HEADER, userId)
                        .param("state", "UNSUPPORTED_STATE"))
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
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(66)
    public void shouldReturnBadRequestOnGetAllWithFrom0Size0() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(USER_HEADER, userId)
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
                        .header(USER_HEADER, userId)
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
                        .header(USER_HEADER, userId)
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
                        .header(USER_HEADER, userId)
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
                        .header(USER_HEADER, userId)
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
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
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

    private BookingRequestDto getBookingRequestDto(long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemId);
        bookingRequestDto.setStart(start);
        bookingRequestDto.setEnd(end);

        return bookingRequestDto;
    }

    private ResponseEntity<Object> getExpectedResponseResult(
            long bookingId,
            ItemResponseDto itemResponseDto,
            UserResponseDto userResponseDto,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return new ResponseEntity<>(getBookingResponseDto(bookingId, itemResponseDto, userResponseDto, start, end),
                HttpStatus.OK);
    }

    private BookingResponseDto getBookingResponseDto(
            long bookingId,
            ItemResponseDto itemResponseDto,
            UserResponseDto userResponseDto,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return BookingResponseDto.builder()
                .id(bookingId)
                .item(itemResponseDto)
                .booker(userResponseDto)
                .start(start)
                .end(end)
                .build();
    }
}
