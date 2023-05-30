package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.PaginationValidator;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;

    @Override
    public Request create(Request request, Long userId) {
        userService.findById(userId);

        request.setUserId(userId);
        request.setCreated(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public Request findById(Long requestId, Long userId) {
        userService.findById(userId);

        Optional<Request> optionalRequest = requestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new RequestNotFoundException(String.format("Request with id %d not found", requestId));
        }

        Request request = optionalRequest.get();
        addItems(request);
        return request;
    }

    @Override
    public Collection<Request> findByUserId(Long userId) {
        userService.findById(userId);

        Collection<Request> requests = requestRepository.findByUserId(userId);
        addItems(requests);
        return requests;
    }

    @Override
    public Collection<Request> findAllWithPagination(Long userId, int from, int size) {
        userService.findById(userId);
        PaginationValidator.validate(from, size);
        int page = from / size;

        Collection<Request> requests = requestRepository.findAllByUserIdNot(userId, PageRequest.of(page, size))
                .getContent();

        addItems(requests);
        return requests;
    }

    private void addItems(Collection<Request> requests) {
        requests.forEach(this::addItems);
    }

    private void addItems(Request request) {
        Collection<Item> items = requestRepository.findItemsByItemRequestId(request.getId());
        items.forEach(i -> i.setRequestId(request.getId()));
        request.setItems(items);
    }
}
