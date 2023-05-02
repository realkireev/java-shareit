package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;
import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create (User user);

    User update (User user, Long userId);

    void delete (Long userId);

    Optional<User> findById (Long userId);

    Optional<User> findByEmail (String email);

    Collection<User> findAll ();
}
