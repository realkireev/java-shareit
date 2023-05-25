package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user1;
    private User user2;
    private User user3;

    @Test
    void testEquals() {
        createUsers();

        assertTrue(user1.equals(user2));
        assertFalse(user1.equals(user3));
    }

    @Test
    void testHashCode() {
        createUsers();

        assertTrue(user1.hashCode() == user2.hashCode());
        assertTrue(user1.hashCode() != user3.hashCode());
    }

    @Test
    void getId() {
        createUsers();
        assertEquals(1, user1.getId());
    }

    @Test
    void getEmail() {
        createUsers();
        assertEquals("jo@gov.com", user1.getEmail());
    }

    @Test
    void getName() {
        createUsers();
        assertEquals("John", user1.getName());
    }

    @Test
    void setId() {
        createUsers();
        user1.setId(7L);
        assertEquals(7, user1.getId());
    }

    @Test
    void setEmail() {
        createUsers();
        user1.setEmail("new_email@dot.com");
        assertEquals("new_email@dot.com", user1.getEmail());
    }

    @Test
    void setName() {
        createUsers();
        user1.setName("new_name");
        assertEquals("new_name", user1.getName());
    }

    @Test
    void builder() {
        User user4 = User.builder().id(4L).name("Rex").email("dog@ya.ru").build();
        User user5 = new User(4L, "dog@ya.ru", "Rex");

        assertTrue(user4.equals(user5));
    }

    private void createUsers() {
        user1 = User.builder()
            .id(1L)
            .name("John")
            .email("jo@gov.com")
            .build();

        user2 = User.builder()
            .id(1L)
            .name("John")
            .email("jo@gov.com")
            .build();

        user3 = User.builder()
            .id(3L)
            .name("Charlie")
            .email("chaplin@serial.com")
            .build();
    }
}