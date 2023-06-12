package ru.practicum.shareit.item.cacheservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.MethodInfo;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemCacheService {
    private final ItemClient itemClient;

    private final Map<MethodInfo, ResponseEntity<Object>> cache = new HashMap<>();

    public ResponseEntity<Object> create(ItemRequestDto itemRequestDto, Long userId) {
        cache.clear();
        return itemClient.create(itemRequestDto, userId);
    }

    public ResponseEntity<Object> update(ItemRequestDto itemRequestDto, Long userId, Long itemId) {
        cache.clear();
        return itemClient.update(itemRequestDto, userId, itemId);
    }

    public ResponseEntity<Object> findByOwnerId(Long userId) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), userId);
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> findById(Long itemId, Long userId) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), itemId,
                userId);
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> search(String text) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), text);
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> delete(Long userId, Long itemId) {
        cache.clear();
        return itemClient.delete(userId, itemId);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        cache.clear();
        return itemClient.addComment(userId, itemId, commentRequestDto);
    }

    private MethodInfo createMethodInfo(String methodName, Object... args) {
        MethodInfo methodInfo = MethodInfo.builder()
                .methodName(methodName)
                .build();

        if (args.length > 0) {
            methodInfo.setArgs(args);
        }

        return methodInfo;
    }

    private ResponseEntity<Object> getFromCache(MethodInfo methodInfo) {
        ResponseEntity<Object> result = null;

        if (cache.containsKey(methodInfo)) {
            return cache.get(methodInfo);
        }

        cache.clear();
        switch (methodInfo.getMethodName()) {
            case "findByOwnerId":
                result = itemClient.findByOwnerId((Long) methodInfo.getArgs()[0]);
                break;

            case "findById":
                result = itemClient.findById((Long) methodInfo.getArgs()[0], (Long) methodInfo.getArgs()[1]);
                break;

            case "search":
                result = itemClient.search((String) methodInfo.getArgs()[0]);
                break;
        }

        cache.put(methodInfo, result);
        return result;
    }
}
