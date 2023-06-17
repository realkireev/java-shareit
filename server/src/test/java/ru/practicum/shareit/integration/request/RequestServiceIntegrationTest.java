package ru.practicum.shareit.integration.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RequestServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    private RequestRequestDto requestRequestDto1;
    private RequestRequestDto requestRequestDto2;
    private UserResponseDto user1;
    private UserResponseDto user2;

    @BeforeEach
    public void preparation() {
        UserRequestDto userRequestDto1 = new UserRequestDto();
        userRequestDto1.setName("Michael Tors");
        userRequestDto1.setEmail("tors@fashion.com");

        UserRequestDto userRequestDto2 = new UserRequestDto();
        userRequestDto2.setName("Bill Clinton");
        userRequestDto2.setEmail("potus@usa.gov");

        requestRequestDto1 = new RequestRequestDto();
        requestRequestDto1.setDescription("A big fridge needed!");

        requestRequestDto2 = new RequestRequestDto();
        requestRequestDto2.setDescription("I need a microscope");

        user1 = userService.create(userRequestDto1);
        user2 = userService.create(userRequestDto2);
    }

    @Test
    public void shouldCreateRequest() {
        RequestResponseDto request = requestService.create(requestRequestDto1, user1.getId());

        assertNotNull(request);
        assertEquals(requestRequestDto1.getDescription(), request.getDescription());
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIsNull() {
        RequestRequestDto requestRequestDto = new RequestRequestDto();
        assertThrows(DataIntegrityViolationException.class, () -> requestService.create(requestRequestDto, user1.getId()));
    }

    @Test
    public void shouldFindRequestById() {
        RequestResponseDto request = requestService.create(requestRequestDto1, user1.getId());
        RequestResponseDto requestFound = requestService.findById(request.getId(), user1.getId());

        assertNotNull(requestFound);
        assertEquals(request.getDescription(), requestFound.getDescription());
    }

    @Test
    public void shouldThrowExceptionWhenFindRequestByUnknownId() {
        assertThrows(RequestNotFoundException.class, () -> requestService.findById(99L, user1.getId()));
    }

    @Test
    public void shouldFindRequestByUserId() {
        RequestResponseDto request = requestService.create(requestRequestDto1, user1.getId());
        List<RequestResponseDto> requestFound = requestService.findByUserId(user1.getId());

        assertNotNull(requestFound);
        assertEquals(1, requestFound.size());
        assertEquals(request.getDescription(), requestFound.get(0).getDescription());
    }

    @Test
    public void shouldFindAllWithPagination() {
        requestService.create(requestRequestDto1, user1.getId());
        requestService.create(requestRequestDto2, user1.getId());
        List<RequestResponseDto> requestFound;

        requestFound = requestService.findAllWithPagination(user1.getId(), 0, 10);
        assertNotNull(requestFound);
        assertEquals(0, requestFound.size());

        requestFound = requestService.findAllWithPagination(user2.getId(), 0, 10);
        assertNotNull(requestFound);
        assertEquals(2, requestFound.size());

        requestFound = requestService.findAllWithPagination(user2.getId(), 0, 1);
        assertNotNull(requestFound);
        assertEquals(1, requestFound.size());
    }
}
