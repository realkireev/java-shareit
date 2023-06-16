package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.RequestRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.common.Variables.USER_HEADER;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @Valid @RequestBody RequestRequestDto requestRequestDto,
            @NotNull @RequestHeader(USER_HEADER) Long userId) {
        return requestClient.create(requestRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findByUserId(
            @NotNull @RequestHeader(USER_HEADER) Long userId) {
        return requestClient.findByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(
            @NotNull @RequestHeader(USER_HEADER) Long userId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
            @Positive @RequestParam(required = false, defaultValue = "20") int size) {
        return requestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(
            @NotNull @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long requestId) {
        return requestClient.findById(requestId, userId);
    }
}
