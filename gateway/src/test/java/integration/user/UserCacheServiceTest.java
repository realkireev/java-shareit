package integration.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.cacheservice.UserCacheService;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserCacheServiceTest.class)
public class UserCacheServiceTest {
    RestTemplate mockRestTemplate = mock(RestTemplate.class);

    private final UserClient userClient = new UserClient(mockRestTemplate);

    private final UserCacheService userCacheService = new UserCacheService(userClient);

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private UserResponseDto userResponseDto2;

    @BeforeEach
    public void preparation() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setName("Michael Jackson");
        userRequestDto.setEmail("mjackson@gmail.com");

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("name")
                .email("e@mail.com")
                .build();

        userResponseDto2 = UserResponseDto.builder()
                .id(2L)
                .name("another name")
                .email("another_e@mail.com")
                .build();
    }

    @Test
    public void testCreateUser() {
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(userResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(userRequestDto);

        when(mockRestTemplate.exchange("", HttpMethod.POST, requestEntity, Object.class)).thenReturn(expectedResult);

        ResponseEntity<Object> result = userCacheService.create(userRequestDto);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("", HttpMethod.POST, requestEntity,
                Object.class);
    }

    @Test
    public void testUpdateUser() {
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(userResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(userRequestDto);
        Map<String, Object> parameters = Map.of("userId", 1L);

        when(mockRestTemplate.exchange("/{userId}", HttpMethod.PATCH, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = userCacheService.update(userRequestDto, 1L);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1))
                .exchange("/{userId}", HttpMethod.PATCH, requestEntity, Object.class, parameters);
    }

    @Test
    public void testDeleteUser() {
        ResponseEntity<Object> expectedResult = ResponseEntity.ok().build();
        HttpEntity<Object> requestEntity = getHttpEntity(null);
        Map<String, Object> parameters = Map.of("userId", 1L);

        when(mockRestTemplate.exchange("/{userId}", HttpMethod.DELETE, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = userCacheService.delete(1L);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1))
                .exchange("/{userId}", HttpMethod.DELETE, requestEntity, Object.class, parameters);
    }

    @Test
    public void testFindAllUsers() {
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(List.of(userResponseDto, userResponseDto2));
        HttpEntity<Object> requestEntity = getHttpEntity(null);

        when(mockRestTemplate.exchange("", HttpMethod.GET, requestEntity, Object.class)).thenReturn(expectedResult);

        ResponseEntity<Object> result = userCacheService.findAll();

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1))
                .exchange("", HttpMethod.GET, requestEntity, Object.class);
    }

    @Test
    public void testFindUserById() {
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(userResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(null);
        Map<String, Object> parameters = Map.of("userId", 1L);

        when(mockRestTemplate.exchange("/{userId}", HttpMethod.GET, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = userCacheService.findById(1L);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1))
                .exchange("/{userId}", HttpMethod.GET, requestEntity, Object.class, parameters);
    }

    private HttpEntity<Object> getHttpEntity(UserRequestDto body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return new HttpEntity<>(body, headers);
    }
}
