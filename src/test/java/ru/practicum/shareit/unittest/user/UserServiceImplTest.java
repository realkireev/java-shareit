package ru.practicum.shareit.unittest.user;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceImplTest {
    @Mock
    private UserRepository mockUserRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void shouldCreateUser() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName("Michael Jackson");
        userRequestDto.setEmail("mjackson@gmail.com");

        User createdUser = User.builder()
                .id(1L)
                .name("Michael Jackson")
                .email("mjackson@gmail.com")
                .build();

        when(mockUserRepository.save(any(User.class))).thenReturn(createdUser);

        UserResponseDto result = userService.create(userRequestDto);

        assertNotNull(result);
        assertEquals(createdUser.getId(), result.getId());
        assertEquals(createdUser.getName(), result.getName());
        assertEquals(createdUser.getEmail(), result.getEmail());

        verify(mockUserRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName("Ivan Demimonde");
        userRequestDto.setEmail("idem@yahoo.com");

        when(mockUserRepository.save(any(User.class))).thenThrow(EmailAlreadyExistsException.class);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(userRequestDto));
        verify(mockUserRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldUpdateUser() {
        Long userId = 1L;

        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName("Steve Jobs");
        userRequestDto.setEmail("jobs@apple.com");

        User storedUser = User.builder()
                .id(userId)
                .name("Tim Cook")
                .email("cook@mail.com")
                .build();

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(storedUser));
        when(mockUserRepository.findByEmailContainingIgnoreCase(anyString())).thenReturn(null);
        when(mockUserRepository.save(any(User.class))).thenReturn(storedUser);

        UserResponseDto updatedUserResponseDto = userService.update(userRequestDto, userId);

        assertNotNull(updatedUserResponseDto);
        assertEquals(userRequestDto.getName(), updatedUserResponseDto.getName());
        assertEquals(userRequestDto.getEmail(), updatedUserResponseDto.getEmail());

        verify(mockUserRepository, times(1)).findById(userId);
        verify(mockUserRepository, times(1)).findByEmailContainingIgnoreCase(anyString());
        verify(mockUserRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenUpdateWithSameEmail() {
        Long userId = 1L;

        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName("John Smith");
        userRequestDto.setEmail("johnsmith@gmail.com");

        User storedUser = User.builder()
                .id(2L)
                .name("Jane Air")
                .email("johnsmith@gmail.com")
                .build();

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(storedUser));
        when(mockUserRepository.findByEmailContainingIgnoreCase(anyString())).thenReturn(Collections.singletonList(storedUser));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.update(userRequestDto, userId));

        verify(mockUserRepository, times(1)).findById(userId);
        verify(mockUserRepository, times(1)).findByEmailContainingIgnoreCase(anyString());
        verify(mockUserRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldDeleteUser() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);
        verify(mockUserRepository, times(1)).findById(userId);
        verify(mockUserRepository, times(1)).delete(user);
    }

    @Test
    public void shouldThrowExceptionWhenDeleteIfUserNotFound() {
        Long userId = 1L;

        when(mockUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.delete(userId));
        verify(mockUserRepository, times(1)).findById(userId);
        verify(mockUserRepository, never()).delete(any(User.class));
    }

    @Test
    public void shouldReturnListOfUser() {
        User user1 = User.builder()
                .id(1L)
                .name("Mary")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Jane")
                .build();

        List<User> userList = Arrays.asList(user1, user2);

        when(mockUserRepository.findAll()).thenReturn(userList);
        List<UserResponseDto> result = userService.findAll();

        assertEquals(userList.size(), result.size());

        for (int i = 0; i < userList.size(); i++) {
            UserResponseDto expected = UserMapper.toUserResponseDto(userList.get(i));
            UserResponseDto actual = result.get(i);
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getName(), actual.getName());
        }

        verify(mockUserRepository, times(1)).findAll();
    }

    @Test
    public void shouldReturnUserDtoById() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .name("Penelope")
                .email("Pen@soap.com")
                .build();

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.findById(userId);
        UserResponseDto expected = UserMapper.toUserResponseDto(user);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getEmail(), result.getEmail());

        verify(mockUserRepository, times(1)).findById(userId);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotFound() {
        Long userId = 1L;

        when(mockUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
        verify(mockUserRepository, times(1)).findById(userId);
    }

    @Test
    public void shouldReturnUserById() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .name("Cassandra")
                .email("cassa@erp.com")
                .build();

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(user));
        User result = userService.findUserById(userId);

        assertEquals(user, result);
        verify(mockUserRepository, times(1)).findById(userId);
    }

}
