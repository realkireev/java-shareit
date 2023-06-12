package controller.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.practicum.shareit.item.cacheservice.ItemCacheService;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.Variables.CONTENT_TYPE;
import static ru.practicum.shareit.common.Variables.USER_HEADER;

@SpringBootTest(classes = { ShareItGateway.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ItemCacheService mockItemCacheService;

    @InjectMocks
    private ItemController itemController;

    private static final String ENDPOINT = "/items";

    @Test
    @Order(1)
    public void testCreateItem() {
        long userId = 1;
        String name = "Item 1";
        String description = "Description of item 1";
        boolean available = true;
        ItemRequestDto itemRequestDto = getItemRequestDto(name, description, available);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, name, description, available);

        when(mockItemCacheService.create(itemRequestDto, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = itemController.create(itemRequestDto, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockItemCacheService, times(1)).create(itemRequestDto, userId);
    }

    @Test
    @Order(2)
    public void shouldReturnBadRequestWithoutOwner() throws Exception {
        String name = "Дрель";
        String description = "Простая дрель";
        Boolean available = true;
        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    public void testUpdateItem() {
        long userId = 1;
        long itemId = 1;
        String name = "Item 1";
        String description = "Description of item 1";
        boolean available = true;
        ItemRequestDto itemRequestDto = getItemRequestDto(name, description, available);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, name, description, available);

        when(mockItemCacheService.update(itemRequestDto, itemId, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = itemController.update(itemRequestDto, itemId, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockItemCacheService, times(1)).update(itemRequestDto, itemId, userId);
    }

    @Test
    @Order(4)
    public void shouldReturnBadRequestWithoutAvailable() throws Exception {
        String name = "Дрель";
        String description = "Простая дрель";
        long ownerId = 1L;

        String body = createJson(name, description, null);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void shouldReturnBadRequestWithEmptyName() throws Exception {
        String name = "";
        String description = "Простая дрель";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void shouldReturnBadRequestWithBlankName() throws Exception {
        String name = "    ";
        String description = "Простая дрель";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void shouldReturnBadRequestWithEmptyDescription() throws Exception {
        String name = "Дрель";
        String description = "";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Order(8)
    @Test
    public void shouldReturnBadRequestWithBlankDescription() throws Exception {
        String name = "Дрель";
        String description = "   ";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    public void testFindItemByOwnerId() {
        long userId = 1;

        String name = "Item 1";
        String description = "Description of item 1";
        boolean available = true;
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, name, description, available);

        when(mockItemCacheService.findByOwnerId(userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = itemController.findByOwnerId(userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockItemCacheService, times(1)).findByOwnerId(userId);
    }

    @Test
    @Order(10)
    public void shouldReturnBadRequestWhileUpdatingWithoutOwner() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель";
        Boolean available = false;
        long expectedId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void testFindItemById() {
        long userId = 1;
        long itemId = 1;

        String name = "Item 1";
        String description = "Description of item 1";
        boolean available = true;
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, name, description, available);

        when(mockItemCacheService.findById(itemId, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = itemController.findById(itemId, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockItemCacheService, times(1)).findById(itemId, userId);
    }

    @Test
    @Order(12)
    public void testSearchItem() {
        String text = "Looking for an item";

        String name = "Item 1";
        String description = "Description of item 1";
        boolean available = true;
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, name, description, available);

        when(mockItemCacheService.search(text)).thenReturn(expectedDto);

        ResponseEntity<Object> result = itemController.search(text);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockItemCacheService, times(1)).search(text);
    }

    @Test
    @Order(13)
    public void testDeleteItem() {
        long itemId = 1;
        long userId = 1;
        ResponseEntity<Object> expectedDto = ResponseEntity.ok().build();

        when(mockItemCacheService.delete(itemId, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = itemController.delete(itemId, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockItemCacheService, times(1)).delete(itemId, userId);
    }

    @Test
    @Order(14)
    public void testAddComment() {
        long itemId = 1;
        long userId = 1;
        ResponseEntity<Object> expectedDto = ResponseEntity.ok().build();
        CommentRequestDto commentRequestDto = new CommentRequestDto();

        when(mockItemCacheService.addComment(userId, itemId, commentRequestDto)).thenReturn(expectedDto);

        ResponseEntity<Object> result = itemController.addComment(userId, itemId, commentRequestDto);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockItemCacheService, times(1)).addComment(userId, itemId, commentRequestDto);
    }

    private String createJson(String name, String description, Boolean available) throws JsonProcessingException {
        Map<String, Object> object = createJsonMap(name, description, available);

        return new ObjectMapper().writeValueAsString(object);
    }

    private Map<String, Object> createJsonMap(String name, String description, Boolean available) {
        Map<String, Object> object = new HashMap<>();
        if (name != null) {
            object.put("name", name);
        }
        if (description != null) {
            object.put("description", description);
        }
        if (available != null) {
            object.put("available", available);
        }

        return object;
    }

    private ItemRequestDto getItemRequestDto(String name, String description, boolean available) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName(name);
        itemRequestDto.setDescription(description);
        itemRequestDto.setAvailable(available);

        return itemRequestDto;
    }

    private ResponseEntity<Object> getExpectedResponseResult(long itemId, String name, String description,
                                                             boolean available) {
        return new ResponseEntity<>(getItemResponseDto(itemId, name, description, available), HttpStatus.OK);
    }

    private ItemResponseDto getItemResponseDto(long itemId, String name, String description, boolean available) {
        return ItemResponseDto.builder()
                .id(itemId)
                .name(name)
                .description(description)
                .available(available)
                .build();
    }
}
