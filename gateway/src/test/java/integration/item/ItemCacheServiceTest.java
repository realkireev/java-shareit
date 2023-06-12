package integration.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.item.cacheservice.ItemCacheService;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ItemCacheServiceTest.class)
public class ItemCacheServiceTest {
    RestTemplate mockRestTemplate = mock(RestTemplate.class);

    private final ItemClient itemClient = new ItemClient(mockRestTemplate);
    private final ItemCacheService itemCacheService = new ItemCacheService(itemClient);

    private ItemRequestDto itemRequestDto;
    private ItemResponseDto itemResponseDto;
    private ItemResponseDto itemResponseDto2;

    @BeforeEach
    public void preparation() {
        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("Item1");
        itemRequestDto.setDescription("Description of Item1");
        itemRequestDto.setAvailable(true);

        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Item1")
                .description("Description of Item1")
                .available(true)
                .build();

        itemResponseDto2 = ItemResponseDto.builder()
                .id(2L)
                .name("Item2")
                .description("Description of Item2")
                .available(false)
                .build();
    }

    @Test
    public void testCreateItem() {
        long userId = 1;
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(itemResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(itemRequestDto, userId);

        when(mockRestTemplate.exchange("", HttpMethod.POST, requestEntity, Object.class)).thenReturn(expectedResult);

        ResponseEntity<Object> result = itemCacheService.create(itemRequestDto, userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("", HttpMethod.POST, requestEntity,
                Object.class);
    }

    @Test
    public void testUpdateItem() {
        long userId = 1;
        long itemId = 1;
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(itemResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(itemRequestDto, userId);
        Map<String, Object> parameters = Map.of("itemId", itemId);

        when(mockRestTemplate.exchange("/{itemId}", HttpMethod.PATCH, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = itemCacheService.update(itemRequestDto, itemId, userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/{itemId}", HttpMethod.PATCH, requestEntity,
                Object.class, parameters);
    }

    @Test
    public void testDeleteItem() {
        long userId = 1;
        long itemId = 1;
        ResponseEntity<Object> expectedResult = ResponseEntity.ok().build();
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = Map.of("itemId", itemId);

        when(mockRestTemplate.exchange("/{itemId}", HttpMethod.DELETE, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = itemCacheService.delete(itemId, userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/{itemId}", HttpMethod.DELETE, requestEntity,
                Object.class, parameters);
    }

    @Test
    public void testFindItemByOwnerId() {
        long userId = 1;
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(itemResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);

        when(mockRestTemplate.exchange("", HttpMethod.GET, requestEntity, Object.class))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = itemCacheService.findByOwnerId(userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("", HttpMethod.GET, requestEntity,
                Object.class);
    }

    @Test
    public void testFindItemById() {
        long userId = 1;
        long itemId = 1;
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(itemResponseDto);
        HttpEntity<Object> requestEntity = getHttpEntity(null, userId);
        Map<String, Object> parameters = Map.of("itemId", itemId);

        when(mockRestTemplate.exchange("/{itemId}", HttpMethod.GET, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = itemCacheService.findById(itemId, userId);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/{itemId}", HttpMethod.GET, requestEntity,
                Object.class, parameters);
    }

    @Test
    public void testSearchItem() {
        String text = "Search IT!";
        ResponseEntity<Object> expectedResult = ResponseEntity.ok(List.of(itemResponseDto, itemResponseDto2));
        HttpEntity<Object> requestEntity = getHttpEntity(null, null);
        Map<String, Object> parameters = Map.of("text", text);

        when(mockRestTemplate.exchange("/search?text={text}", HttpMethod.GET, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = itemCacheService.search(text);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/search?text={text}", HttpMethod.GET, requestEntity,
                Object.class, parameters);
    }

    @Test
    public void testAddComment() {
        long userId = 1;
        long itemId = 1;
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("This is a comment!");
        ResponseEntity<Object> expectedResult = ResponseEntity.ok().build();
        HttpEntity<Object> requestEntity = getHttpEntity(commentRequestDto, userId);
        Map<String, Object> parameters = Map.of("itemId", itemId);


        when(mockRestTemplate.exchange("/{itemId}/comment", HttpMethod.POST, requestEntity, Object.class, parameters))
                .thenReturn(expectedResult);

        ResponseEntity<Object> result = itemCacheService.addComment(userId, itemId, commentRequestDto);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(mockRestTemplate, times(1)).exchange("/{itemId}/comment", HttpMethod.POST, requestEntity,
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
