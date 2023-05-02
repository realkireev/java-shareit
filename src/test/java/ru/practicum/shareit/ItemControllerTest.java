package ru.practicum.shareit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static final String ENDPOINT = "/items";
    private static final String CONTENT_TYPE = "application/json";
    private static final String HEADER_OWNER_ID = "X-Sharer-User-Id";

    @Test
    public void test001ShouldCreateCorrectItem() throws Exception {
        String name = "Дрель";
        String description = "Простая дрель";
        Boolean available = true;
        long ownerId = 1L;
        long expectedId = 1L;
        String body = createJson(name, description, available);

        createUser(1);
        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test002ShouldReturnBadRequestWithoutOwner() throws Exception {
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
    public void test003ShouldReturnNotFoundWithUnknownUser() throws Exception {
        String name = "Дрель";
        String description = "Простая дрель";
        Boolean available = true;
        long ownerId = 999L;
        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void test004ShouldReturnBadRequestWithoutAvailable() throws Exception {
        String name = "Дрель";
        String description = "Простая дрель";
        long ownerId = 1L;

        String body = createJson(name, description, null);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test005ShouldReturnBadRequestWithEmptyName() throws Exception {
        String name = "";
        String description = "Простая дрель";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test006ShouldReturnBadRequestWithBlankName() throws Exception {
        String name = "    ";
        String description = "Простая дрель";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test007ShouldReturnBadRequestWithEmptyDescription() throws Exception {
        String name = "Дрель";
        String description = "";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test008ShouldReturnBadRequestWithBlankDescription() throws Exception {
        String name = "Дрель";
        String description = "   ";
        Boolean available = true;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    public void test009ShouldUpdateFirstItem() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель";
        Boolean available = false;
        long expectedId = 1L;
        long ownerId = 1L;

        String body = createJson(name, description, available);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test010ShouldReturnBadRequestWhileUpdatingWithoutOwner() throws Exception {
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
    public void test011ShouldReturnForbiddenWhileUpdatingWithWrongOwner() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель";
        Boolean available = false;
        long expectedId = 1L;
        long ownerId = 999L;

        String body = createJson(name, description, available);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void test012ShouldReturnFirstItemUnavailable() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель";
        Boolean available = false;
        long expectedId = 1L;
        long ownerId = 1L;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test013ShouldUpdateFirstItemOnlyAvailable() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        String body = createJson(null, null, available);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test014ShouldReturnFirstItemAvailable() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test015ShouldUpdateFirstItemOnlyDescription() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель + аккумулятор";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        String body = createJson(null, description, null);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test016ShouldReturnFirstItemWithUpdatedDescription() throws Exception {
        String name = "Дрель+";
        String description = "Аккумуляторная дрель + аккумулятор";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test017ShouldUpdateFirstItemOnlyName() throws Exception {
        String name = "Аккумуляторная дрель";
        String description = "Аккумуляторная дрель + аккумулятор";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        String body = createJson(name, null, null);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test018ShouldReturnFirstItemWithUpdatedName() throws Exception {
        String name = "Аккумуляторная дрель";
        String description = "Аккумуляторная дрель + аккумулятор";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        mockMvc.perform(get(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test019ShouldCreateSecondItem() throws Exception {
        String name = "Отвертка";
        String description = "Аккумуляторная отвертка";
        Boolean available = true;
        long expectedId = 2L;
        long ownerId = 2L;
        String body = createJson(name, description, available);

        createUser(2);
        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test020ShouldReturnItemsOfFirstOwner() throws Exception {
        String name = "Аккумуляторная дрель";
        String description = "Аккумуляторная дрель + аккумулятор";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(expectedId))
                .andExpect(jsonPath("$[0].name").value(name))
                .andExpect(jsonPath("$[0].description").value(description))
                .andExpect(jsonPath("$[0].available").value(available));
    }

    @Test
    public void test021ShouldReturnItemsOfSecondOwner() throws Exception {
        String name = "Отвертка";
        String description = "Аккумуляторная отвертка";
        Boolean available = true;
        long expectedId = 2L;
        long ownerId = 2L;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(expectedId))
                .andExpect(jsonPath("$[0].name").value(name))
                .andExpect(jsonPath("$[0].description").value(description))
                .andExpect(jsonPath("$[0].available").value(available));
    }

    @Test
    public void test022ShouldSearchAndReturnTwoItems() throws Exception {
        String searchText = "аккУМУляторная";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Аккумуляторная дрель"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная дрель + аккумулятор"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Отвертка"))
                .andExpect(jsonPath("$[1].description").value("Аккумуляторная отвертка"))
                .andExpect(jsonPath("$[1].available").value(true));
    }

    @Test
    public void test023ShouldSearchAndReturnOneItem() throws Exception {
        String searchText = "ОТВЕРтка";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Отвертка"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная отвертка"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    public void test024ShouldSearchAndReturnNoItems() throws Exception {
        String searchText = "Астролябия";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void test025ShouldUpdateSecondItemUnavailable() throws Exception {
        String name = "Отвертка";
        String description = "Аккумуляторная отвертка";
        Boolean available = false;
        long expectedId = 2L;
        long ownerId = 2L;

        String body = createJson(null, null, available);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test026ShouldSearchAndReturnOneItem() throws Exception {
        String searchText = "дрЕЛЬ";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Аккумуляторная дрель"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная дрель + аккумулятор"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    public void test027ShouldSearchAndReturnOneAvailableItem() throws Exception {
        String searchText = "аккУМУляторная";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Аккумуляторная дрель"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная дрель + аккумулятор"))
                .andExpect(jsonPath("$[0].available").value(true));
    }


    @Test
    public void test028ShouldUpdateSecondItemAvailable() throws Exception {
        String name = "Отвертка";
        String description = "Аккумуляторная отвертка";
        Boolean available = true;
        long expectedId = 2L;
        long ownerId = 2L;

        String body = createJson(null, null, available);

        mockMvc.perform(patch(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }

    @Test
    public void test029ShouldSearchAndReturnTwoAvailableItems() throws Exception {
        String searchText = "аккУМУляторная";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Аккумуляторная дрель"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная дрель + аккумулятор"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Отвертка"))
                .andExpect(jsonPath("$[1].description").value("Аккумуляторная отвертка"))
                .andExpect(jsonPath("$[1].available").value(true));
    }

    @Test
    public void test030ShouldSearchAndReturnOneAvailableItem() throws Exception {
        String searchText = "оТверТ";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Отвертка"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная отвертка"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    public void test031ShouldSearchAndReturnNoItems() throws Exception {
        String searchText = "";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void test032ShouldDeleteSecondItem() throws Exception {
        long expectedId = 2L;
        long ownerId = 2L;

        mockMvc.perform(delete(ENDPOINT + "/" + expectedId)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test033ShouldReturnOneItemOfFirstOwner() throws Exception {
        String name = "Аккумуляторная дрель";
        String description = "Аккумуляторная дрель + аккумулятор";
        Boolean available = true;
        long expectedId = 1L;
        long ownerId = 1L;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(expectedId))
                .andExpect(jsonPath("$[0].name").value(name))
                .andExpect(jsonPath("$[0].description").value(description))
                .andExpect(jsonPath("$[0].available").value(available));
    }


    @Test
    public void test034ShouldReturnNoItemsOfSecondOwner() throws Exception {
        long ownerId = 2L;

        mockMvc.perform(get(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void test035ShouldSearchAndReturnNoItems() throws Exception {
        String searchText = "ОТВЕРтка";

        mockMvc.perform(get(ENDPOINT + "/search")
                        .queryParam("text", searchText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void test036ShouldCreateItemIgnoreIdFromRequest() throws Exception {
        String name = "Струнный мотатель";
        String description = "Великолепный мотатель фирмы Gibson";
        Boolean available = true;
        long ownerId = 3L;
        long expectedId = 3L;
        Long requestedId = 999L;

        String body = createJson(requestedId, name, description, available);

        createUser(3);
        mockMvc.perform(post(ENDPOINT)
                        .header(HEADER_OWNER_ID, ownerId)
                        .contentType(CONTENT_TYPE)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available));
    }


    private String createJson(Object id, String name, String description, Boolean available) throws JsonProcessingException {
        Map<String, Object> object = createJsonMap(name, description, available);
        object.put("id", id);

        return new ObjectMapper().writeValueAsString(object);
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

    private void createUser(long id) throws Exception {
        String body = String.format("{\"name\":\"An owner\",\"email\": \"just_%d@owner.de\"}", id);
        mockMvc.perform(post("/users")
                .contentType(CONTENT_TYPE)
                .content(body))
                .andDo(print());
    }
}
