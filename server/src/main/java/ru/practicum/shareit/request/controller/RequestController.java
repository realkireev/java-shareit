package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

import static ru.practicum.shareit.common.Variables.USER_HEADER;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    public RequestResponseDto create(
            @RequestBody RequestRequestDto requestRequestDto,
            @RequestHeader(USER_HEADER) Long userId) {

        return requestService.create(requestRequestDto, userId);
    }

    @GetMapping
    public List<RequestResponseDto> findByUserId(
            @RequestHeader(USER_HEADER) Long userId) {

        return requestService.findByUserId(userId);
    }

    @GetMapping("/all")
    public List<RequestResponseDto> findAllWithPagination(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam int from,
            @RequestParam int size) {

        return requestService.findAllWithPagination(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestResponseDto findById(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long requestId) {

        return requestService.findById(requestId, userId);
    }
}
