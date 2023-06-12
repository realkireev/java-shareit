package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponseDto create(UserRequestDto userRequestDto) {
        User user = UserMapper.toUser(userRequestDto);
        User createdUser;

        try {
            createdUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(String.format("Email %s already exists!", user.getEmail()));
        }

        return UserMapper.toUserResponseDto(createdUser);
    }

    @Override
    public UserResponseDto update(UserRequestDto userRequestDto, Long userId) {
        User storedUser = getUserByIdOrThrowException(userId);
        User user = UserMapper.toUser(userRequestDto);
        String email = user.getEmail();

        List<User> users = userRepository.findByEmailContainingIgnoreCase(email);
        if (users != null) {
            if (users.stream().anyMatch(x -> !Objects.equals(x.getId(), userId))) {
                throw new EmailAlreadyExistsException(String.format("Email %s already exists!", email));
            }
        }

        String newName = user.getName();
        String newEmail = user.getEmail();

        if (newName != null) {
            storedUser.setName(newName);
        }

        if (newEmail != null) {
            storedUser.setEmail(newEmail);
        }

        return UserMapper.toUserResponseDto(userRepository.save(storedUser));
    }

    @Override
    public void delete(Long userId) {
        User user = getUserByIdOrThrowException(userId);
        userRepository.delete(user);
    }

    @Override
    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream().map(UserMapper::toUserResponseDto).collect(Collectors.toList());
    }

    @Override
    public UserResponseDto findById(Long userId) {
        return UserMapper.toUserResponseDto(findUserById(userId));
    }

    @Override
    public User findUserById(Long userId) {
        return getUserByIdOrThrowException(userId);
    }

    private User getUserByIdOrThrowException(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }
    }
}
