package integration.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.request.cacheservice.RequestCacheService;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RequestCacheServiceTest.class)
public class RequestCacheServiceTest {
    RestTemplate mockRestTemplate = mock(RestTemplate.class);

    private final RequestClient requestClient = new RequestClient(mockRestTemplate);

    private final RequestCacheService requestCacheService = new RequestCacheService(requestClient);

    private RequestRequestDto requestRequestDto;
    private RequestResponseDto requestResponseDto;
    private RequestResponseDto requestResponseDto2;

    @BeforeEach
    public void preparation() {
        requestRequestDto = new RequestRequestDto();
        requestRequestDto.setDescription("Barbeque is needed!");

        requestResponseDto = RequestResponseDto.builder()
                .id(1L)
                .items(Collections.emptyList())
                .description("Description")
                .build();

        requestResponseDto2 = RequestResponseDto.builder()
                .id(2L)
                .items(Collections.emptyList())
                .description("A very long description")
                .build();
    }

    @Test
    public void testCreateBooking() {
        long userId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(requestResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(requestRequestDto, userId);

        when(mockRestTemplate.exchange("", HttpMethod.POST, requestEntity, Object.class)).thenReturn(expectedResult);

        ResponseEntity<Object> result = requestCacheService.create(requestRequestDto, userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("", HttpMethod.POST, requestEntity,
                Object.class);
    }

    @Test
    public void testFindAllBookings() {
        long userId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(List.of(requestResponseDto, requestResponseDto2));
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("from", 0);
        parameters.put("size", 20);

        when(mockRestTemplate.exchange("/all?from={from}&size={size}", HttpMethod.GET, requestEntity,
                Object.class, parameters)).thenReturn(expectedResult);

        ResponseEntity<Object> result = requestCacheService.findAll(userId, 0, 20);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/all?from={from}&size={size}", HttpMethod.GET,
                requestEntity, Object.class, parameters);
    }

    @Test
    public void testFindBookingsByUserId() {
        long userId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(List.of(requestResponseDto, requestResponseDto2));
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);

        when(mockRestTemplate.exchange("", HttpMethod.GET, requestEntity,
                Object.class)).thenReturn(expectedResult);

        ResponseEntity<Object> result = requestCacheService.findByUserId(userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("", HttpMethod.GET, requestEntity, Object.class);
    }

    @Test
    public void testFindRequestById() {
        long userId = 1;
        long requestId = 1;

        ResponseEntity<Object> expectedResult = ResponseEntity.ok(requestResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = Map.of(
                "requestId", requestId
        );

        when(mockRestTemplate.exchange("/{requestId}", HttpMethod.GET, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = requestCacheService.findById(requestId, userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/{requestId}", HttpMethod.GET, requestEntity,
                Object.class, parameters);
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
