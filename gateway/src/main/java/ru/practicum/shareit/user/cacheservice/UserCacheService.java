package ru.practicum.shareit.user.cacheservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.MethodInfo;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final UserClient userClient;

    private final Map<MethodInfo, ResponseEntity<Object>> cache = new HashMap<>();

    public ResponseEntity<Object> findAll() {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName());
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> findById(long userId) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), userId);
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> create(UserRequestDto userRequestDto) {
        cache.clear();
        return userClient.create(userRequestDto);
    }

    public ResponseEntity<Object> update(UserRequestDto userRequestDto, Long userId) {
        cache.clear();
        return userClient.update(userRequestDto, userId);
    }

    public ResponseEntity<Object> delete(Long userId) {
        cache.clear();
        return userClient.delete(userId);
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
            case "findAll":
                result = userClient.findAll();
                break;

            case "findById":
                result = userClient.findById((Long) methodInfo.getArgs()[0]);
                break;
        }

        cache.put(methodInfo, result);
        return result;
    }
}
