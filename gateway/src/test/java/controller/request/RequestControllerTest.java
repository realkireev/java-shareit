package controller.request;

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
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
public class RequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RequestClient mockRequestClient;

    @InjectMocks
    private RequestController requestController;

    private static final String ENDPOINT = "/requests";

    @Test
    @Order(1)
    public void testCreateRequest() {
        long userId = 1;
        String description = "Test-Request";
        RequestRequestDto requestRequestDto = getRequestRequestDto(description);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, description);

        when(mockRequestClient.create(requestRequestDto, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = requestController.create(requestRequestDto, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockRequestClient, times(1)).create(any(RequestRequestDto.class), anyLong());
    }

    @Test
    @Order(2)
    public void shouldReturnBadRequestOnPostRequestWithEmptyDescription() throws Exception {
        long userId = 1;
        String body = "{\"description\": \"\"}";

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    public void shouldReturnBadRequestOnPostRequestWithNullDescription() throws Exception {
        long userId = 1;
        String body = "{\"description\": null}";

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void shouldReturnBadRequestOnPostRequestWithoutDescription() throws Exception {
        long userId = 1;
        String body = "{}";

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void shouldReturnBadRequestOnPostRequestWithoutBody() throws Exception {
        long userId = 1;
        String body = "";

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void testFindRequestsByUserId() {
        long userId = 1;
        String description = "Test-Request";
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, description);

        when(mockRequestClient.findByUserId(userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = requestController.findByUserId(userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockRequestClient, times(1)).findByUserId(anyLong());
    }

    @Test
    @Order(7)
    public void testFindAllRequests() {
        long userId = 1;
        String description = "Test-Request";
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(1, description);

        when(mockRequestClient.findAll(userId, 0, 20)).thenReturn(expectedDto);

        ResponseEntity<Object> result = requestController.findAll(userId, 0, 20);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockRequestClient, times(1)).findAll(anyLong(), anyInt(), anyInt());
    }

    @Test
    @Order(8)
    public void testRequestById() {
        long requestId = 1;
        long userId = 1;
        String description = "Test-Request";
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(requestId, description);

        when(mockRequestClient.findById(requestId, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = requestController.findById(requestId, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(mockRequestClient, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    @Order(9)
    public void shouldReturnBadRequestOnGetAllRequestsWithFrom0Size0() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    public void shouldReturnBadRequestOnGetAllRequestsWithFromNegativeSize20() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/all")
                        .header(USER_HEADER, userId)
                        .param("from", "-1")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void shouldReturnBadRequestOnGetAllRequestsWithFrom0SizeNegative() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private RequestRequestDto getRequestRequestDto(String description) {
        RequestRequestDto requestRequestDto = new RequestRequestDto();
        requestRequestDto.setDescription(description);

        return requestRequestDto;
    }

    private ResponseEntity<Object> getExpectedResponseResult(long requestId, String description) {
        return new ResponseEntity<>(getRequestResponseDto(requestId, description), HttpStatus.OK);
    }

    private RequestResponseDto getRequestResponseDto(long requestId, String description) {
        return RequestResponseDto.builder()
                .id(requestId)
                .description(description)
                .items(Collections.emptyList())
                .build();
    }
}
