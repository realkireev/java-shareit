package ru.practicum.shareit.booking.cacheservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.common.MethodInfo;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingCacheService {
    private final BookingClient bookingClient;

    private final Map<MethodInfo, ResponseEntity<Object>> cache = new HashMap<>();

    public ResponseEntity<Object> create(Long userId, BookingRequestDto bookingRequestDto) {
        cache.clear();
        return bookingClient.create(userId, bookingRequestDto);
    }

    public ResponseEntity<Object> approve(Long bookingId, Long userId, boolean approved) {
        cache.clear();
        return bookingClient.approve(bookingId, userId, approved);
    }

    public ResponseEntity<Object> findById(Long userId, Long bookingId) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), userId,
                bookingId);
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> findAllByUserIdAndState(Long userId, String state, int from, int size) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), userId,
                state, from, size);
        return getFromCache(methodInfo);
    }

    public ResponseEntity<Object> findAllByOwnerIdAndState(Long userId, String state, int from, int size) {
        MethodInfo methodInfo = createMethodInfo(Thread.currentThread().getStackTrace()[1].getMethodName(), userId,
                state, from, size);
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
            case "findById":
                result = bookingClient.findById((Long) methodInfo.getArgs()[0], (Long) methodInfo.getArgs()[1]);
                break;

            case "findAllByUserIdAndState":
                result = bookingClient.findAllByUserIdAndState(
                        (Long) methodInfo.getArgs()[0],
                        (String) methodInfo.getArgs()[1],
                        (Integer) methodInfo.getArgs()[2],
                        (Integer) methodInfo.getArgs()[3]);
                break;

            case "findAllByOwnerIdAndState":
                result = bookingClient.findAllByOwnerIdAndState(
                        (Long) methodInfo.getArgs()[0],
                        (String) methodInfo.getArgs()[1],
                        (Integer) methodInfo.getArgs()[2],
                        (Integer) methodInfo.getArgs()[3]);
                break;
        }

        cache.put(methodInfo, result);
        return result;
    }
}
