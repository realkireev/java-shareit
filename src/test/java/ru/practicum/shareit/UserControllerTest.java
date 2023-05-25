package ru.practicum.shareit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
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

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static final String ENDPOINT = "/users";
    private static final String CONTENT_TYPE = "application/json";

    @Test
    @Order(1)
    public void shouldCreateCorrectUser() throws Exception {
        String name = "Ivan Ivanov";
        String email = "cool@hacker.ru";
        long expectedId = 1L;
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
    public void shouldReturnConflictWithDuplicatedEmail() throws Exception {
        String body = createJson("John Johnson", "cool@hacker.ru");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    public void shouldReturnBadRequestWithoutEmail() throws Exception {
        String body = createJson("Somebody", null);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void shouldReturnBadRequestWithBadEmail() throws Exception {
        String body = createJson("Somebody", "smb.com");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void shouldUpdateFirstUser() throws Exception {
        String name = "Ivan Ivanovich Ivanov";
        String email = "advanced@developer.ru";
        long expectedId = 1L;

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
    @Order(6)
    public void shouldCreateCorrectSecondUser() throws Exception {
        String name = "James Hetfield";
        String email = "admin@metallica.com";
        long expectedId = 3L;
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
    @Order(7)
    public void shouldUpdateFirstUserOnlyName() throws Exception {
        String name = "Professor Ivanov";
        String email = "advanced@developer.ru";
        long expectedId = 1L;


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
    @Order(8)
    public void shouldUpdateFirstUserOnlyEmail() throws Exception {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long expectedId = 1L;

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
    @Order(9)
    public void shouldUpdateFirstUserWithSameEmail() throws Exception {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long expectedId = 1L;

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
    @Order(10)
    public void shouldReturnConflictWhenUpdateWithDuplicatedEmail() throws Exception {
        String name = "Professor Ivanov";
        String email = "admin@metallica.com";
        long expectedId = 1L;
        String body = createJson(name, email);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Order(11)
    public void shouldReturnFirstUser() throws Exception {
        String name = "Professor Ivanov";
        String email = "ceo@it.com";
        long expectedId = 1L;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(12)
    public void shouldReturnSecondUser() throws Exception {
        String name = "James Hetfield";
        String email = "admin@metallica.com";
        long expectedId = 3L;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(13)
    public void shouldReturnTwoUsers2() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Order(14)
    public void shouldDeleteSecondUser() throws Exception {
        long expectedId = 3L;

        mockMvc.perform(delete(ENDPOINT + "/" + expectedId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(15)
    public void shouldReturnOneUser() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Order(16)
    public void shouldCreateAnotherUser() throws Exception {
        String name = "Jason Statham";
        String email = "noone@knows.com";
        long expectedId = 4L;
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
    @Order(17)
    public void shouldReturnTwoUsers() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Order(18)
    public void shouldCreateUserAndIgnoreIdFromRequest() throws Exception {
        String name = "Lady Gaga";
        String email = "info@music.com";
        Long requestId = 999L;
        long expectedId = 5L;
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
    @Order(19)
    public void shouldReturnThreeUsers() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Professor Ivanov"))
                .andExpect(jsonPath("$[0].email").value("ceo@it.com"))
                .andExpect(jsonPath("$[1].id").value(4))
                .andExpect(jsonPath("$[1].name").value("Jason Statham"))
                .andExpect(jsonPath("$[1].email").value("noone@knows.com"))
                .andExpect(jsonPath("$[2].id").value(5))
                .andExpect(jsonPath("$[2].name").value("Lady Gaga"))
                .andExpect(jsonPath("$[2].email").value("info@music.com"));
    }

    @Test
    @Order(20)
    public void shouldReturnUserNotFound() throws Exception {
        long expectedId = 682L;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId))
                .andDo(print())
                .andExpect(status().isNotFound());
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
