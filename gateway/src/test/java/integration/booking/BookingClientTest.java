package integration.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.dto.RequestBookingState;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BookingClientTest.class)
public class BookingClientTest {
    RestTemplate mockRestTemplate = mock(RestTemplate.class);

    private final BookingClient bookingClient = new BookingClient(mockRestTemplate);

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private BookingResponseDto bookingResponseDto2;

    @BeforeEach
    public void preparation() {
        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(2));

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .item(ItemResponseDto.builder().build())
                .booker(UserResponseDto.builder().build())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.WAITING)
                .build();

        bookingResponseDto2 = BookingResponseDto.builder()
                .id(2L)
                .item(ItemResponseDto.builder().build())
                .booker(UserResponseDto.builder().build())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    public void testCreateBooking() {
        long userId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(bookingResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(bookingRequestDto, userId);

        when(mockRestTemplate.exchange("", HttpMethod.POST, requestEntity, Object.class)).thenReturn(expectedResult);

        ResponseEntity<Object> result = bookingClient.create(userId, bookingRequestDto);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("", HttpMethod.POST, requestEntity,
                Object.class);
    }

    @Test
    public void testApproveBooking() {
        long userId = 1;
        long bookingId = 1;
        boolean approved = true;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(bookingResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = Map.of(
                "bookingId", bookingId,
                "approved", approved
        );

        when(mockRestTemplate.exchange("/{bookingId}?approved={approved}", HttpMethod.PATCH, requestEntity,
                Object.class, parameters)).thenReturn(expectedResult);

        ResponseEntity<Object> result = bookingClient.approve(bookingId, userId, approved);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/{bookingId}?approved={approved}", HttpMethod.PATCH,
                requestEntity, Object.class, parameters);
    }

    @Test
    public void testFindBookingById() {
        long userId = 1;
        long bookingId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(bookingResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = Map.of(
                "bookingId", bookingId
        );

        when(mockRestTemplate.exchange("/{bookingId}", HttpMethod.GET, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = bookingClient.findById(userId, bookingId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/{bookingId}", HttpMethod.GET,
                requestEntity, Object.class, parameters);
    }

    @Test
    public void testFindAllBookingByUserIdAndState() {
        long userId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(List.of(bookingResponseDto, bookingResponseDto2));
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = Map.of(
                "state", RequestBookingState.ALL,
                "from", 0,
                "size", 20
        );

        when(mockRestTemplate.exchange("?state={state}&from={from}&size={size}", HttpMethod.GET, requestEntity,
                Object.class, parameters)).thenReturn(expectedResult);

        ResponseEntity<Object> result = bookingClient.findAllByUserIdAndState(userId, "ALL", 0, 20);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("?state={state}&from={from}&size={size}", HttpMethod.GET,
                requestEntity, Object.class, parameters);
    }

    @Test
    public void testFindAllBookingByOwnerIdAndState() {
        long userId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(List.of(bookingResponseDto, bookingResponseDto2));
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = Map.of(
                "state", RequestBookingState.ALL,
                "from", 0,
                "size", 20
        );

        when(mockRestTemplate.exchange("/owner?state={state}&from={from}&size={size}", HttpMethod.GET, requestEntity,
                Object.class, parameters)).thenReturn(expectedResult);

        ResponseEntity<Object> result = bookingClient.findAllByOwnerIdAndState(userId, "ALL", 0, 20);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/owner?state={state}&from={from}&size={size}", HttpMethod.GET,
                requestEntity, Object.class, parameters);
    }

    private HttpEntity<Object> getHttpEntity(Object body, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }

        return new HttpEntity<>(body, headers);
    }
}
