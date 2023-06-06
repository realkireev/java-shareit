package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.Variables.USER_HEADER;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
@Validated
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    public RequestResponseDto create(
            @Valid @RequestBody RequestRequestDto requestRequestDto,
            @NotNull @RequestHeader(USER_HEADER) Long userId) {

        return requestService.create(requestRequestDto, userId);
    }

    @GetMapping
    public List<RequestResponseDto> findByUserId(
            @NotNull @RequestHeader(USER_HEADER) Long userId) {

        return requestService.findByUserId(userId);
    }

    @GetMapping("/all")
    public List<RequestResponseDto> findAllWithPagination(
            @NotNull @RequestHeader(USER_HEADER) Long userId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
            @Positive @RequestParam(required = false, defaultValue = "20") int size) {

        return requestService.findAllWithPagination(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestResponseDto findById(
            @NotNull @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long requestId) {

        return requestService.findById(requestId, userId);
    }
}
