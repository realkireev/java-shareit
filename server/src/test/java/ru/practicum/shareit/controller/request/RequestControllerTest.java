package ru.practicum.shareit.controller.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.Variables.CONTENT_TYPE;
import static ru.practicum.shareit.common.Variables.USER_HEADER;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static final String ENDPOINT = "/requests";
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final List<User> users = createUserObjects();

    private final Request request1 = Request.builder()
            .id(1L)
            .description("Срочно ищу малиновый пиджак")
            .build();

    private final Item item1 = Item.builder()
            .id(1L)
            .name("Дрель")
            .description("Электрическая дрель")
            .owner(users.get(0))
            .available(true)
            .build();

    private final Item item2 = Item.builder()
            .id(2L)
            .name("Отвертка")
            .description("Двуручная отвертка")
            .owner(users.get(3))
            .available(false)
            .build();

    private final Item item3 = Item.builder()
            .id(3L)
            .name("Самосвал")
            .description("Трехколесный")
            .owner(users.get(3))
            .available(true)
            .build();

    private final Item item4 = Item.builder()
            .id(4L)
            .name("Граммофон")
            .description("С новыми иголками!")
            .owner(users.get(5))
            .available(true)
            .build();

    private final Item item5 = Item.builder()
            .id(5L)
            .name("Крутой малиновый пиджак")
            .description("Отдаю по запросу")
            .owner(users.get(4))
            .available(true)
            .requestId(1L)
            .build();

    @Test
    @Order(1)
    public void shouldReturnNotFoundOnPostRequestWithUnknownUser() throws Exception {
        sendRequestsToCreateEntities();
        long userId = 99;
        String body = "{\"description\": \"Срочно ищу малиновый пиджак\"}";

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    public void shouldReturnNotFoundOnGetRequestWithUnknownUser() throws Exception {
        long userId = 99;

        mockMvc.perform(get(ENDPOINT)
                        .header(USER_HEADER, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    public void shouldReturnEmptyListOnGetRequestWithUserWithoutRequests() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(USER_HEADER, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(12)
    public void shouldReturnEmptyOnGetAllRequestsWithFrom0Size20() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(13)
    public void shouldCreateRequest1() throws Exception {
        long userId = 1;
        String body = "{\"description\": \"Срочно ищу малиновый пиджак\"}";

        mockMvc.perform(post(ENDPOINT)
                        .header(USER_HEADER, userId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request1.getId()))
                .andExpect(jsonPath("$.description").value(request1.getDescription()))
                .andExpect(jsonPath("$.created").value(notNullValue()))
                .andExpect(jsonPath("$.created").value(greaterThan(LocalDateTime.now().minusSeconds(2).toString())))
                .andExpect(jsonPath("$.created").value(lessThan(LocalDateTime.now().toString())));
    }

    @Test
    @Order(14)
    public void shouldReturnRequest1() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT)
                        .header(USER_HEADER, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(request1.getId()))
                .andExpect(jsonPath("$[0].description").value(request1.getDescription()))
                .andExpect(jsonPath("$[0].created").value(notNullValue()))
                .andExpect(jsonPath("$[0].items").value(notNullValue()))
                .andExpect(jsonPath("$[0].items", hasSize(0)));
    }

    @Test
    @Order(15)
    public void shouldReturnRequest1WithItem5() throws Exception {
        sendItemToDatabase(item5);

        long userId = 1;
        mockMvc.perform(get(ENDPOINT)
                        .header(USER_HEADER, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(request1.getId()))
                .andExpect(jsonPath("$[0].description").value(request1.getDescription()))
                .andExpect(jsonPath("$[0].created").value(notNullValue()))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id").value(item5.getId()))
                .andExpect(jsonPath("$[0].items[0].name").value(item5.getName()))
                .andExpect(jsonPath("$[0].items[0].description").value(item5.getDescription()))
                .andExpect(jsonPath("$[0].items[0].available").value(item5.getAvailable()))
                .andExpect(jsonPath("$[0].items[0].requestId").value(request1.getId()));
    }

    @Test
    @Order(16)
    public void shouldReturnEmptyOnGetAllRequestsWithFrom0Size20ForRequestOwner() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(17)
    public void shouldReturnRequest1OnGetAllRequestsWithFrom0Size20ForOtherUser() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(request1.getId()))
                .andExpect(jsonPath("$[0].description").value(request1.getDescription()))
                .andExpect(jsonPath("$[0].created").value(notNullValue()))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id").value(item5.getId()))
                .andExpect(jsonPath("$[0].items[0].name").value(item5.getName()))
                .andExpect(jsonPath("$[0].items[0].description").value(item5.getDescription()))
                .andExpect(jsonPath("$[0].items[0].available").value(item5.getAvailable()))
                .andExpect(jsonPath("$[0].items[0].requestId").value(request1.getId()));
    }

    @Test
    @Order(18)
    public void shouldReturnNotFoundOnGetRequestByIdForUnknownUser() throws Exception {
        long userId = 99;

        mockMvc.perform(get(ENDPOINT + "/1")
                        .header(USER_HEADER, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(19)
    public void shouldReturnNotFoundOnGetRequestByUnknownId() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/99")
                        .header(USER_HEADER, userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(20)
    public void shouldReturnRequest1OnGetRequest1WithFrom0Size20ForOtherUser() throws Exception {
        long userId = 4;

        mockMvc.perform(get(ENDPOINT + "/1")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request1.getId()))
                .andExpect(jsonPath("$.description").value(request1.getDescription()))
                .andExpect(jsonPath("$.created").value(notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(item5.getId()))
                .andExpect(jsonPath("$.items[0].name").value(item5.getName()))
                .andExpect(jsonPath("$.items[0].description").value(item5.getDescription()))
                .andExpect(jsonPath("$.items[0].available").value(item5.getAvailable()))
                .andExpect(jsonPath("$.items[0].requestId").value(request1.getId()));
    }

    @Test
    @Order(21)
    public void shouldReturnRequest1OnGetRequest1WithFrom0Size20ForRequestOwner() throws Exception {
        long userId = 1;

        mockMvc.perform(get(ENDPOINT + "/1")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request1.getId()))
                .andExpect(jsonPath("$.description").value(request1.getDescription()))
                .andExpect(jsonPath("$.created").value(notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(item5.getId()))
                .andExpect(jsonPath("$.items[0].name").value(item5.getName()))
                .andExpect(jsonPath("$.items[0].description").value(item5.getDescription()))
                .andExpect(jsonPath("$.items[0].available").value(item5.getAvailable()))
                .andExpect(jsonPath("$.items[0].requestId").value(request1.getId()));
    }

    private List<User> createUserObjects() {
        List<User> users = new ArrayList<>();

        for (long i = 1; i <= 6; i++) {
            User user = User.builder()
                    .id(i)
                    .name(String.format("User%d", i))
                    .email(String.format("user%d@mail.com", i))
                    .build();
            users.add(user);
        }
        return users;
    }

    private void sendUserToDatabase(User user) {
        try {
            String body = objectMapper.writeValueAsString(user);

            mockMvc.perform(post("/users")
                    .contentType(CONTENT_TYPE)
                    .content(body));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendItemToDatabase(Item item) throws Exception {
        String body = objectMapper.writeValueAsString(item);

        mockMvc.perform(post("/items")
                .header(USER_HEADER, item.getOwner().getId())
                .contentType(CONTENT_TYPE)
                .content(body));
    }

    private void sendRequestsToCreateEntities() throws Exception {
        users.forEach(this::sendUserToDatabase);

        sendItemToDatabase(item1);
        sendItemToDatabase(item2);
        sendItemToDatabase(item3);
        sendItemToDatabase(item4);
    }
}
