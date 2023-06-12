package ru.practicum.shareit.request.cacheservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.MethodInfo;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.RequestRequestDto;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequestCacheService {
    private final RequestClient requestClient;

    private final Map<MethodInfo, ResponseEntity<Object>> cache = new HashMap<>();

    public ResponseEntity<Object> create(RequestRequestDto requestRequestDto, Long userId) {
        cache.clear();
        return requestClient.create(requestRequestDto, userId);
    }

    public ResponseEntity<Object> findByUserId(Long userId) {
        cache.clear();
        return requestClient.findByUserId(userId);
    }

    public ResponseEntity<Object> findAll(Long userId, int from, int size) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), userId,
                from, size);
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> findById(Long requestId, Long userId) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), requestId,
                userId);
        return getFromCache(methodInfo);
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
            case "findByUserId":
                result = requestClient.findByUserId((Long) methodInfo.getArgs()[0]);
                break;

            case "findById":
                result = requestClient.findById((Long) methodInfo.getArgs()[0], (Long) methodInfo.getArgs()[1]);
                break;

            case "findAll":
                result = requestClient.findAll(
                        (Long) methodInfo.getArgs()[0],
                        (Integer) methodInfo.getArgs()[1],
                        (Integer) methodInfo.getArgs()[2]);
                break;
        }

        cache.put(methodInfo, result);
        return result;
    }
}
