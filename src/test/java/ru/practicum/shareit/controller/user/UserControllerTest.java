package ru.practicum.shareit.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.Variables.CONTENT_TYPE;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static final String ENDPOINT = "/users";

    @Test
    @Order(1)
    public void shouldCreateCorrectUser() throws Exception {
        String name = "Ivan Ivanov";
        String email = "cool@hacker.ru";
        long expectedId = 1;
        String body = createJson(name, email);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(2)
    public void shouldUpdateFirstUser() throws Exception {
        String name = "Ivan Ivanovich Ivanov";
        String email = "advanced@developer.ru";
        long expectedId = 1;

        String body = createJson(name, email);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(3)
    public void shouldCreateCorrectSecondUser() throws Exception {
        String name = "James Hetfield";
        String email = "admin@metallica.com";
        long expectedId = 2;
        String body = createJson(name, email);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(4)
    public void shouldUpdateFirstUserOnlyName() throws Exception {
        String name = "Professor Ivanov";
        String email = "advanced@developer.ru";
        long expectedId = 1;

        String body = createJson(name, null);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(5)
    public void shouldUpdateFirstUserOnlyEmail() throws Exception {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long expectedId = 1;

        String body = createJson(null, email);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(6)
    public void shouldUpdateFirstUserWithSameEmail() throws Exception {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long expectedId = 1;

        String body = createJson(null, email);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(7)
    public void shouldReturnFirstUser() throws Exception {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long expectedId = 1;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(8)
    public void shouldReturnSecondUser() throws Exception {
        String name = "James Hetfield";
        String email = "admin@metallica.com";
        long expectedId = 2;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(9)
    public void shouldReturnTwoUsers2() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Order(10)
    public void shouldDeleteSecondUser() throws Exception {
        long expectedId = 2;

        mockMvc.perform(delete(ENDPOINT + "/" + expectedId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    public void shouldReturnOneUser() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Order(12)
    public void shouldCreateAnotherUser() throws Exception {
        String name = "Jason Statham";
        String email = "noone@knows.com";
        long expectedId = 3;
        String body = createJson(name, email);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(13)
    public void shouldReturnTwoUsers() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Order(14)
    public void shouldCreateUserAndIgnoreIdFromRequest() throws Exception {
        String name = "Lady Gaga";
        String email = "info@music.com";
        long requestId = 999;
        long expectedId = 4;
        String body = createJson(requestId, name, email);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(15)
    public void shouldReturnThreeUsers() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Professor Ivanov"))
                .andExpect(jsonPath("$[0].email").value("ceo@it.com"))
                .andExpect(jsonPath("$[1].id").value(3))
                .andExpect(jsonPath("$[1].name").value("Jason Statham"))
                .andExpect(jsonPath("$[1].email").value("noone@knows.com"))
                .andExpect(jsonPath("$[2].id").value(4))
                .andExpect(jsonPath("$[2].name").value("Lady Gaga"))
                .andExpect(jsonPath("$[2].email").value("info@music.com"));
    }

    @Nested
    @DisplayName("Returns 404 Not Found")
    class ShouldReturnNotFound {
        @Test
        @Order(1)
        public void shouldReturnUserNotFound() throws Exception {
            long expectedId = 682;

            mockMvc.perform(get(ENDPOINT + "/" + expectedId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Returns 409 Conflict")
    class ShouldReturnConflicts {
        @BeforeEach
        public void preparation() throws Exception {
            String body = createJson("John Johnson", "cool@hacker.ru");

            mockMvc.perform(post(ENDPOINT)
                            .contentType(CONTENT_TYPE)
                            .content(body))
                    .andDo(print());

            body = createJson("Peter Peterson", "peterson@hacker.ru");

            mockMvc.perform(post(ENDPOINT)
                            .contentType(CONTENT_TYPE)
                            .content(body))
                    .andDo(print());
        }

        @Test
        @Order(1)
        public void shouldReturnConflictWithDuplicatedEmail() throws Exception {
            String body = createJson("John Johnson", "cool@hacker.ru");

            mockMvc.perform(post(ENDPOINT)
                            .contentType(CONTENT_TYPE)
                            .content(body))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @Order(2)
        public void shouldReturnConflictWhenUpdateWithDuplicatedEmail() throws Exception {
            String name = "Professor Ivanov";
            String email = "cool@hacker.ru";
            long expectedId = 2;
            String body = createJson(name, email);

            mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                            .contentType(CONTENT_TYPE)
                            .content(body))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Returns 400 Bad Request")
    class ShouldReturnBadRequest {
        @Test
        @Order(1)
        public void shouldReturnBadRequestWithoutEmail() throws Exception {
            String body = createJson("Somebody", null);

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

    private String createJson(Object id, String name, String email) throws JsonProcessingException {
        Map<String, Object> object = createJsonMap(name, email);
        object.put("id", id);

        return new ObjectMapper().writeValueAsString(object);
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
