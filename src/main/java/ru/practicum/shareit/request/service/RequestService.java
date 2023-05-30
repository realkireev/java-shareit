package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.Request;
import java.util.Collection;

public interface RequestService {
    Request create(Request request, Long userId);

    Request findById(Long requestId, Long userId);

    Collection<Request> findByUserId(Long userId);

    Collection<Request> findAllWithPagination(Long userId, int from, int size);
}
