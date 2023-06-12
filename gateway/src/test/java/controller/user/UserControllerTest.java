package controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
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
import ru.practicum.shareit.user.cacheservice.UserCacheService;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.Variables.CONTENT_TYPE;

@SpringBootTest(classes = { ShareItGateway.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserCacheService userCacheService;

    @InjectMocks
    private UserController userController;

    private static final String ENDPOINT = "/users";

    @Test
    @Order(1)
    public void shouldCreateCorrectUser() {
        long userId = 1;
        String name = "Ivan Ivanov";
        String email = "cool@hacker.ru";
        UserRequestDto userRequestDto = getUserRequestDto(name, email);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(userId, name, email);

        when(userCacheService.create(userRequestDto)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.create(userRequestDto);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).create(any(UserRequestDto.class));
    }

    @Test
    @Order(2)
    public void shouldUpdateFirstUser() {
        long userId = 1;
        String name = "Ivan Ivanovich Ivanov";
        String email = "advanced@developer.ru";
        UserRequestDto userRequestDto = getUserRequestDto(name, email);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(userId, name, email);

        when(userCacheService.update(userRequestDto, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.update(userRequestDto, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).update(any(UserRequestDto.class), anyLong());
    }

    @Test
    @Order(3)
    public void shouldCreateCorrectSecondUser() {
        String name = "James Hetfield";
        String email = "admin@metallica.com";
        long userId = 2;
        UserRequestDto userRequestDto = getUserRequestDto(name, email);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(userId, name, email);

        when(userCacheService.create(userRequestDto)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.create(userRequestDto);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).create(any(UserRequestDto.class));
    }

    @Test
    @Order(4)
    public void shouldUpdateFirstUserOnlyName() {
        String name = "Professor Ivanov";
        String email = "advanced@developer.ru";
        long userId = 1;
        UserRequestDto userRequestDto = getUserRequestDto(name, email);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(userId, name, email);

        when(userCacheService.update(userRequestDto, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.update(userRequestDto, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).update(any(UserRequestDto.class), anyLong());
    }

    @Test
    @Order(5)
    public void shouldUpdateFirstUserOnlyEmail() {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long userId = 1;
        UserRequestDto userRequestDto = getUserRequestDto(name, email);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(userId, name, email);

        when(userCacheService.update(userRequestDto, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.update(userRequestDto, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).update(any(UserRequestDto.class), anyLong());
    }

    @Test
    @Order(6)
    public void shouldUpdateFirstUserWithSameEmail() {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long userId = 1;
        UserRequestDto userRequestDto = getUserRequestDto(name, email);
        ResponseEntity<Object> expectedDto = getExpectedResponseResult(userId, name, email);

        when(userCacheService.update(userRequestDto, userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.update(userRequestDto, userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).update(any(UserRequestDto.class), anyLong());
    }

    @Test
    @Order(7)
    public void shouldReturnFirstUser() {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long userId = 1;

        ResponseEntity<Object> expectedDto = getExpectedResponseResult(userId, name, email);

        when(userCacheService.findById(userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.findById(userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).findById(anyLong());
    }

    @Test
    @Order(9)
    public void shouldReturnTwoUsers2() {
        UserResponseDto user1 = getUserResponseDto(1L, "Name1", "e1@mail.com");
        UserResponseDto user2 = getUserResponseDto(2L, "Name2", "e2@mail.com");

        ResponseEntity<Object> expectedDto = new ResponseEntity<>(List.of(user1, user2), HttpStatus.OK);

        when(userCacheService.findAll()).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.findAll();

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).findAll();
    }

    @Test
    @Order(10)
    public void shouldDeleteSecondUser() throws Exception {
        long userId = 1;
        ResponseEntity<Object> expectedDto = ResponseEntity.ok().build();

        when(userCacheService.delete(userId)).thenReturn(expectedDto);

        ResponseEntity<Object> result = userController.delete(userId);

        assertEquals(result.getBody(), expectedDto.getBody());
        assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
        verify(userCacheService, times(1)).delete(anyLong());
    }


    @Nested
    @DisplayName("Returns 404 Not Found")
    class ShouldReturnNotFound {
        @Test
        @Order(1)
        public void shouldReturnUserNotFound() throws Exception {
            long userId = 682;
            ResponseEntity<Object> expectedDto = ResponseEntity.notFound().build();
            when(userCacheService.findById(userId)).thenReturn(expectedDto);

            ResponseEntity<Object> result = userController.findById(userId);

            assertEquals(result.getBody(), expectedDto.getBody());
            assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
            verify(userCacheService, times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("Returns 409 Conflict")
    class ShouldReturnConflicts {
        private final ResponseEntity<Object> expectedDto = ResponseEntity.status(409).build();
        private final UserRequestDto userRequestDto = getUserRequestDto("Username", "e@mail.com");

        @Test
        @Order(1)
        public void shouldReturnConflictWithDuplicatedEmail() throws Exception {
            when(userCacheService.create(userRequestDto)).thenReturn(expectedDto);

            ResponseEntity<Object> result = userController.create(userRequestDto);

            assertEquals(result.getBody(), expectedDto.getBody());
            assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
            verify(userCacheService, times(1)).create(any(UserRequestDto.class));
        }

        @Test
        @Order(2)
        public void shouldReturnConflictWhenUpdateWithDuplicatedEmail() throws Exception {
            when(userCacheService.update(userRequestDto, 1L)).thenReturn(expectedDto);

            ResponseEntity<Object> result = userController.update(userRequestDto, 1L);

            assertEquals(result.getBody(), expectedDto.getBody());
            assertEquals(result.getStatusCode(), expectedDto.getStatusCode());
            verify(userCacheService, times(1)).update(any(UserRequestDto.class), anyLong());
        }
    }

    @Nested
    @DisplayName("Returns 400 Bad Request")
    class ShouldReturnBadRequest {
        @Test
        @Order(1)
        public void shouldReturnBadRequestWithoutEmail() throws Exception {
            String body = createJson("Nobody", null);

            mockMvc.perform(post(ENDPOINT)
                            .contentType(CONTENT_TYPE)
                            .content(body))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(2)
        public void shouldReturnBadRequestWithBadEmail() throws Exception {
            String body = createJson("Somebody", "smb.com");

            mockMvc.perform(post(ENDPOINT)
                            .contentType(CONTENT_TYPE)
                            .content(body))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    private UserRequestDto getUserRequestDto(String name, String email) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName(name);
        userRequestDto.setEmail(email);

        return userRequestDto;
    }

    private ResponseEntity<Object> getExpectedResponseResult(long userId, String name, String email) {
        return new ResponseEntity<>(getUserResponseDto(userId, name, email), HttpStatus.OK);
    }

    private UserResponseDto getUserResponseDto(long userId, String name, String email) {
        return UserResponseDto.builder()
                .id(userId)
                .name(name)
                .email(email)
                .build();
    }

    private String createJson(String name, String email) throws JsonProcessingException {
        Map<String, Object> object = createJsonMap(name, email);

        return new ObjectMapper().writeValueAsString(object);
    }

    private Map<String, Object> createJsonMap(String name, String email) {
        Map<String, Object> object = new HashMap<>();
        if (name != null) {
            object.put("name", name);
        }
        if (email != null) {
            object.put("email", email);
        }

        return object;
    }
}
