package ru.practicum.shareit.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;
import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findByEmailContainingIgnoreCase(String email);
}
