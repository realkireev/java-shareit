package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final RequestService requestService;

    @PostMapping
    public RequestResponseDto create(
            @Valid @RequestBody RequestRequestDto requestRequestDto,
            @NotNull @RequestHeader(USER_HEADER) Long userId) {

        return RequestMapper.toItemRequestResponseDto(requestService.create(RequestMapper.toItemRequest(
                requestRequestDto), userId));
    }

    @GetMapping
    public Collection<RequestResponseDto> findByUserId(
            @NotNull @RequestHeader(USER_HEADER) Long userId) {
        return requestService.findByUserId(userId).stream()
                .map(RequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public Collection<RequestResponseDto> findAllWithPagination(
            @NotNull @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "1000") int size) {
        return requestService.findAllWithPagination(userId, from, size).stream()
                .map(RequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{requestId}")
    public RequestResponseDto findById(
            @NotNull @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long requestId) {
        return RequestMapper.toItemRequestResponseDto(requestService.findById(requestId, userId));
    }
}
