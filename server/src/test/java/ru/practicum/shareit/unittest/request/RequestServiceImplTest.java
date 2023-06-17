package ru.practicum.shareit.unittest.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.RequestRequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RequestServiceImplTest.class)
public class RequestServiceImplTest {
    @Mock
    private RequestRepository mockRequestRepository;

    @Mock
    private UserService mockUserService;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private RequestRequestDto requestRequestDto1;
    private Request request1;
    private List<RequestResponseDto> requestResponseDtos;

    @BeforeEach
    public void preparation() {
        user = User.builder()
                .id(1L)
                .name("John Travolta")
                .email("travolta@hollywood.com")
                .build();

        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        requestRequestDto1 = new RequestRequestDto();
        requestRequestDto1.setDescription("I need a microwave oven!");

        RequestRequestDto requestRequestDto2 = new RequestRequestDto();
        requestRequestDto2.setDescription("I need a giraffe");

        request1 = Request.builder()
                .id(1L)
                .description(requestRequestDto1.getDescription())
                .userId(user.getId())
                .created(LocalDateTime.now())
                .build();

        Request request2 = Request.builder()
                .id(2L)
                .description(requestRequestDto2.getDescription())
                .userId(user.getId())
                .created(LocalDateTime.now())
                .build();

        List<Request> requests = List.of(request1, request2);

        RequestResponseDto requestResponseDto1 = RequestResponseDto.builder()
                .id(request1.getId())
                .description(request1.getDescription())
                .created(request1.getCreated())
                .build();

        RequestResponseDto requestResponseDto2 = RequestResponseDto.builder()
                .id(request2.getId())
                .description(request2.getDescription())
                .created(request2.getCreated())
                .build();

        requestResponseDtos = List.of(requestResponseDto1, requestResponseDto2);

        when(mockUserService.findById(user.getId())).thenReturn(userResponseDto);
        when(mockRequestRepository.save(any(Request.class))).thenReturn(request1);
        when(mockRequestRepository.findById(request1.getId())).thenReturn(Optional.of(request1));
        when(mockRequestRepository.findByUserId(user.getId())).thenReturn(requests);
        when(mockRequestRepository.findAllByUserIdNot(eq(user.getId()), any(Pageable.class))).thenReturn(requests);
    }

    @Test
    public void shouldCreateRequest() {
        RequestResponseDto requestResponseDto = requestService.create(requestRequestDto1, user.getId());

        assertNotNull(requestResponseDto);
        assertEquals(requestResponseDto.getDescription(), requestRequestDto1.getDescription());

        verify(mockUserService, times(1)).findById(user.getId());
        verify(mockRequestRepository, times(1)).save(any(Request.class));
    }

    @Test
    public void shouldFindRequestById() {
        RequestResponseDto requestResponseDto = requestService.findById(request1.getId(), user.getId());

        assertNotNull(requestResponseDto);
        assertEquals(requestResponseDto.getId(), request1.getId());
        assertEquals(requestResponseDto.getDescription(), request1.getDescription());
        assertEquals(requestResponseDto.getCreated(), request1.getCreated());

        verify(mockUserService, times(1)).findById(user.getId());
        verify(mockRequestRepository, times(1)).findById(request1.getId());
    }

    @Test
    public void shouldFindRequestByUserId() {
        List<RequestResponseDto> result = requestService.findByUserId(user.getId());

        commonRequestAsserts(result);
        verify(mockUserService, times(1)).findById(user.getId());
        verify(mockRequestRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    public void shouldFindAllRequestsWithPagination() {
        int from = 0;
        int size = 10;

        List<RequestResponseDto> result = requestService.findAllWithPagination(user.getId(), from, size);

        commonRequestAsserts(result);
        verify(mockUserService, times(1)).findById(user.getId());
        verify(mockRequestRepository, times(1)).findAllByUserIdNot(eq(user.getId()), any(Pageable.class));
    }

    private void commonRequestAsserts(List<RequestResponseDto> result) {
        assertNotNull(result);
        assertEquals(requestResponseDtos.size(), result.size());

        IntStream.range(0, result.size()).forEach(i -> {
            assertEquals(requestResponseDtos.get(i).getId(), result.get(i).getId());
            assertEquals(requestResponseDtos.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(requestResponseDtos.get(i).getCreated(), result.get(i).getCreated());
        });
    }
}
