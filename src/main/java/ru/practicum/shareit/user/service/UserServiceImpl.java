package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        User createdUser;

        try {
            createdUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(String.format("Email %s already exists!", user.getEmail()));
        }

        return createdUser;
    }

    @Override
    public User update(User user, Long userId) {
        User storedUser = getUserByIdOrThrowException(userId);
        String email = user.getEmail();

        if (userRepository.findByEmailContainingIgnoreCase(email)
                .stream()
                .anyMatch(x -> !Objects.equals(x.getId(), userId))) {
            throw new EmailAlreadyExistsException(String.format("Email %s already exists!", email));
        }

        String newName = user.getName();
        String newEmail = user.getEmail();

        if (newName != null) {
            storedUser.setName(newName);
        }

        if (newEmail != null) {
            storedUser.setEmail(newEmail);
        }

        return userRepository.save(storedUser);
    }

    @Override
    public void delete(Long userId) {
        User user = getUserByIdOrThrowException(userId);
        userRepository.delete(user);
    }

    @Override
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long userId) {
        return getUserByIdOrThrowException(userId);
    }

    private User getUserByIdOrThrowException(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }
    }
}
