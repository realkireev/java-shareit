package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserResponseDto create(UserRequestDto userRequestDto);

    UserResponseDto update(UserRequestDto userRequestDto, Long userId);

    void delete(Long userId);

    List<UserResponseDto> findAll();

    UserResponseDto findById(Long userId);

    User findUserById(Long userId);
}
