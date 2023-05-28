package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserRequestDto userRequestDto) {
        return UserMapper.toUserResponseDto(userService.create(UserMapper.toUser(userRequestDto)));
    }

    @PatchMapping("/{userId}")
    public UserResponseDto update(
            @RequestBody UserRequestDto userRequestDto,
            @PathVariable Long userId) {
        return UserMapper.toUserResponseDto(userService.update(UserMapper.toUser(userRequestDto), userId));
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }

    @GetMapping
    public Collection<UserResponseDto> findAll() {
        return userService.findAll().stream()
                .map(UserMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public UserResponseDto findById(@PathVariable Long userId) {
        return UserMapper.toUserResponseDto(userService.findById(userId));
    }
}
