package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import java.util.*;

@Component
public class UserMemoryStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 1L;

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(x -> x.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public User create(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user, Long userId) {
        return users.put(userId, user);
    }

    @Override
    public void delete(Long userId) {
       users.remove(userId);
    }

    @Override
    public Optional<User> findById(Long userId) {
        if (users.containsKey(userId)) {
            return Optional.of(users.get(userId));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }
}
