package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.MethodInfo;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final Map<MethodInfo, List<RequestResponseDto>> cache = new HashMap<>();

    @Override
    public RequestResponseDto create(RequestRequestDto requestRequestDto, Long userId) {
        userService.findById(userId);
        Request request = RequestMapper.toRequest(requestRequestDto);

        request.setUserId(userId);
        request.setCreated(LocalDateTime.now());

        cache.clear();
        return RequestMapper.toRequestResponseDto(requestRepository.save(request));
    }

    @Override
    public RequestResponseDto findById(Long requestId, Long userId) {
        userService.findById(userId);

        MethodInfo methodInfo = new MethodInfo("findById", requestId, userId);
        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo).get(0);
        }

        Optional<Request> optionalRequest = requestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new RequestNotFoundException(String.format("Request with id %d not found", requestId));
        }

        Request request = optionalRequest.get();
        addItems(request);

        RequestResponseDto result = RequestMapper.toRequestResponseDto(request);
        cache.put(methodInfo, List.of(result));

        return result;
    }

    @Override
    public List<RequestResponseDto> findByUserId(Long userId) {
        userService.findById(userId);

        MethodInfo methodInfo = new MethodInfo("findByUserId", userId);
        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo);
        }

        List<Request> requests = requestRepository.findByUserId(userId);
        addItems(requests);

        List<RequestResponseDto> result = requests.stream()
                .map(RequestMapper::toRequestResponseDto)
                .collect(Collectors.toList());
        cache.put(methodInfo, result);

        return result;
    }

    @Override
    public List<RequestResponseDto> findAllWithPagination(Long userId, int from, int size) {
        userService.findById(userId);

        MethodInfo methodInfo = new MethodInfo("findAllWithPagination", userId, from, size);
        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo);
        }

        int page = from / size;

        List<Request> requests = requestRepository.findAllByUserIdNot(userId, PageRequest.of(page, size));

        addItems(requests);

        List<RequestResponseDto> result = requests.stream()
                .map(RequestMapper::toRequestResponseDto)
                .collect(Collectors.toList());
        cache.put(methodInfo, result);

        return result;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    private void addItems(Collection<Request> requests) {
        requests.forEach(this::addItems);
    }

    private void addItems(Request request) {
        List<Item> items = requestRepository.findItemsByRequestId(request.getId());
        items.forEach(i -> i.setRequestId(request.getId()));
        request.setItems(items);
    }
}
