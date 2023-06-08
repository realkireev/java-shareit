package ru.practicum.shareit.repository.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void preparation() {
        user1 = User.builder()
                .name("Jack")
                .email("jackie@mail.ya")
                .build();

        user2 = User.builder()
                .name("Daniel")
                .email("daniels@mail.ru")
                .build();

        user3 = User.builder()
                .name("Winston")
                .email("churchill@mail.ya")
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));
    }

    @Test
    public void testFindUsersByEmailContainingIgnoreCase() {
        List<User> foundUsers = userRepository.findByEmailContainingIgnoreCase("mAIl");

        assertEquals(3, foundUsers.size());
        assertTrue(foundUsers.contains(user1));
        assertTrue(foundUsers.contains(user2));
        assertTrue(foundUsers.contains(user3));
    }

    @Test
    public void testFindUsersByEmailContainingIgnoreCase2() {
        List<User> foundUsers = userRepository.findByEmailContainingIgnoreCase("Ru");

        assertEquals(1, foundUsers.size());
        assertTrue(foundUsers.contains(user2));
    }
}
