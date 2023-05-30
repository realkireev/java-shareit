package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.request.model.Request;
import java.util.stream.Collectors;

public class RequestMapper {
    public static Request toItemRequest(RequestRequestDto requestRequestDto) {
        return Request.builder()
                .description(requestRequestDto.getDescription())
                .build();
    }

    public static RequestResponseDto toItemRequestResponseDto(Request request) {
        RequestResponseDto requestResponseDto;

        requestResponseDto = RequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();

        if (request.getItems() != null) {
            requestResponseDto.setItems(request.getItems().stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList()));
        }

        return requestResponseDto;
    }
}
