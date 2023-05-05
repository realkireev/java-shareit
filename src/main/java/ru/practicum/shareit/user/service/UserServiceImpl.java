package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public User create(User user) {
        String email = user.getEmail();

        if (userStorage.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(String.format("Email %s already exists!", email));
        }
        return userStorage.create(user);
    }

    @Override
    public User update(User user, Long userId) {
        checkUserExists(userId);
        String email = user.getEmail();

        if (userStorage.findByEmail(email).filter(x -> !Objects.equals(x.getId(), userId)).isPresent()) {
            throw new EmailAlreadyExistsException(String.format("Email %s already exists!", email));
        }

        Optional<User> optionalUser = userStorage.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }
        User storedUser = optionalUser.get();

        String newName = user.getName();
        String newEmail = user.getEmail();

        if (newName != null) {
            storedUser.setName(newName);
        }

        if (newEmail != null) {
            storedUser.setEmail(newEmail);
        }

        return userStorage.update(storedUser, userId);
    }

    @Override
    public void delete(Long userId) {
        checkUserExists(userId);
        userStorage.delete(userId);
    }

    @Override
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    @Override
    public User findById(Long userId) {
        Optional<User> optionalUser = userStorage.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }

        return optionalUser.get();
    }

    private void checkUserExists(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }
    }
}
