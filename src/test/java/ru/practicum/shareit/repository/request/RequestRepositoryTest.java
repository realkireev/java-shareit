package ru.practicum.shareit.repository.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RequestRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    private Request request1;
    private Request request2;
    private Request request3;

    @BeforeEach
    public void preparation() {
        User user1 = User.builder()
                .id(1L)
                .name("Arnold")
                .email("arn@yahoo.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Bobbie")
                .email("bob@yahoo.com")
                .build();

        request1 = Request.builder()
                .description("I need a tablet!")
                .userId(1L)
                .created(LocalDateTime.now())
                .build();

        request2 = Request.builder()
                .description("Seeking for a microphone :)")
                .userId(2L)
                .created(LocalDateTime.now())
                .build();

        request3 = Request.builder()
                .description("A wedding suit for one night")
                .userId(2L)
                .created(LocalDateTime.now())
                .build();

        userRepository.saveAll(List.of(user1, user2));
        requestRepository.saveAll(List.of(request1, request2, request3));
    }

    @Test
    public void testFindAllRequestsExceptUserId() {
         List<Request> result = requestRepository.findAllByUserIdNot(2L, PageRequest.of(0, 10));

         assertNotNull(result);
         assertEquals(1, result.size());
         assertEquals(1, result.get(0).getUserId());
         assertEquals(request1.getDescription(), result.get(0).getDescription());
    }

    @Test
    public void testFindRequestsByUserId() {
        List<Request> result = requestRepository.findByUserId(2L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getUserId());
        assertEquals(request2.getDescription(), result.get(0).getDescription());
        assertEquals(2, result.get(1).getUserId());
        assertEquals(request3.getDescription(), result.get(1).getDescription());
    }
}
