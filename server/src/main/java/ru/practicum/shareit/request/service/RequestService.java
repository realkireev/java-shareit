package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.util.List;

public interface RequestService {
    RequestResponseDto create(RequestRequestDto request, Long userId);

    RequestResponseDto findById(Long requestId, Long userId);

    List<RequestResponseDto> findByUserId(Long userId);

    List<RequestResponseDto> findAllWithPagination(Long userId, int from, int size);
}
