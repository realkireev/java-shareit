package ru.practicum.shareit.integration.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    private UserRequestDto userRequestDto1;
    private UserRequestDto userRequestDto2;
    private UserRequestDto userRequestDto3;

    @BeforeEach
    public void preparation() {
        userRequestDto1 = new UserRequestDto();
        userRequestDto1.setName("Eddie Murphy");
        userRequestDto1.setEmail("ed@admin.su");

        userRequestDto2 = new UserRequestDto();
        userRequestDto2.setName("Arnie");
        userRequestDto2.setEmail("arnie@schwarz.de");

        userRequestDto3 = new UserRequestDto();
        userRequestDto3.setName("Tim Roth");
        userRequestDto3.setEmail("arnie@schwarz.de");
    }

    @Test
    public void shouldCreateUser() {
        UserResponseDto createdUser = userService.create(userRequestDto1);

        assertNotNull(createdUser);
        assertEquals(userRequestDto1.getName(), createdUser.getName());
        assertEquals(userRequestDto1.getEmail(), createdUser.getEmail());
    }

    @Test
    public void shouldThrowExceptionWithSameEmail() {
        userService.create(userRequestDto2);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(userRequestDto3));
    }

    @Test
    public void shouldUpdateUser() {
        UserResponseDto createdUser = userService.create(userRequestDto1);
        assertNotNull(createdUser);

        Long userId = createdUser.getId();

        UserResponseDto updatedUser = userService.update(userRequestDto2, userId);

        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals(userRequestDto2.getName(), updatedUser.getName());
        assertEquals(userRequestDto2.getEmail(), updatedUser.getEmail());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateUserWithExistingEmail() {
        UserResponseDto createdUser1 = userService.create(userRequestDto1);
        UserResponseDto createdUser2 = userService.create(userRequestDto2);

        assertNotNull(createdUser1);
        assertNotNull(createdUser2);

        Long userId1 = createdUser1.getId();

        userRequestDto2.setEmail(userRequestDto1.getEmail());

        assertThrows(EmailAlreadyExistsException.class, () -> userService.update(userRequestDto3, userId1));
    }

    @Test
    public void shouldDeleteUser() {
        UserResponseDto createdUser = userService.create(userRequestDto1);

        assertNotNull(createdUser);
        Long userId = createdUser.getId();

        userService.delete(userId);

        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    public void shouldFindAllUsers() {
        userService.create(userRequestDto1);
        userService.create(userRequestDto2);

        List<UserResponseDto> users = userService.findAll();

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    public void shouldFindUserById() {
        UserResponseDto createdUser = userService.create(userRequestDto1);

        assertNotNull(createdUser);
        Long userId = createdUser.getId();

        UserResponseDto foundUser = userService.findById(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals(userRequestDto1.getName(), foundUser.getName());
        assertEquals(userRequestDto1.getEmail(), foundUser.getEmail());
    }
}
