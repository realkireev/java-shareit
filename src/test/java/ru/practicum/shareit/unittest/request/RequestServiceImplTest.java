package ru.practicum.shareit.unittest.request;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RequestServiceImplTest {
    @Mock
    private RequestRepository mockRequestRepository;

    @Mock
    private UserService mockUserService;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private UserResponseDto userResponseDto;
    private RequestRequestDto requestRequestDto1;
    private Request request1;
    private List<Request> requests;
    private List<RequestResponseDto> requestResponseDtos;

    @Test
    public void shouldCreateRequest() {
        createTestObjects();

        when(mockUserService.findById(user.getId())).thenReturn(userResponseDto);
        when(mockRequestRepository.save(any(Request.class))).thenReturn(request1);

        RequestResponseDto requestResponseDto = requestService.create(requestRequestDto1, user.getId());

        assertNotNull(requestResponseDto);
        assertEquals(requestResponseDto.getDescription(), requestRequestDto1.getDescription());

        verify(mockUserService, times(1)).findById(user.getId());
        verify(mockRequestRepository, times(1)).save(any(Request.class));
    }

    @Test
    public void shouldFindRequestById() {
        createTestObjects();
        Optional<Request> optionalRequest = Optional.of(request1);

        when(mockUserService.findById(user.getId())).thenReturn(userResponseDto);
        when(mockRequestRepository.findById(request1.getId())).thenReturn(optionalRequest);

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
        createTestObjects();

        when(mockUserService.findById(user.getId())).thenReturn(userResponseDto);
        when(mockRequestRepository.findByUserId(user.getId())).thenReturn(requests);

        List<RequestResponseDto> result = requestService.findByUserId(user.getId());

        assertNotNull(result);
        assertEquals(requestResponseDtos.size(), result.size());
        assertEquals(requestResponseDtos.get(0).getId(), result.get(0).getId());
        assertEquals(requestResponseDtos.get(0).getDescription(), result.get(0).getDescription());
        assertEquals(requestResponseDtos.get(0).getCreated(), result.get(0).getCreated());

        assertEquals(requestResponseDtos.get(1).getId(), result.get(1).getId());
        assertEquals(requestResponseDtos.get(1).getDescription(), result.get(1).getDescription());
        assertEquals(requestResponseDtos.get(1).getCreated(), result.get(1).getCreated());

        verify(mockUserService, times(1)).findById(user.getId());
        verify(mockRequestRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    public void shouldFindAllRequestsWithPagination() {
        createTestObjects();
        int from = 0;
        int size = 10;
        PageImpl<Request> requestPage = new PageImpl<>(requests);

        when(mockUserService.findById(user.getId())).thenReturn(userResponseDto);
        when(mockRequestRepository.findAllByUserIdNot(eq(user.getId()), any(Pageable.class))).thenReturn(requestPage);

        List<RequestResponseDto> result = requestService.findAllWithPagination(user.getId(), from, size);

        assertNotNull(result);
        assertEquals(requestResponseDtos.size(), result.size());
        assertEquals(requestResponseDtos.get(0).getId(), result.get(0).getId());
        assertEquals(requestResponseDtos.get(0).getDescription(), result.get(0).getDescription());
        assertEquals(requestResponseDtos.get(0).getCreated(), result.get(0).getCreated());

        assertEquals(requestResponseDtos.get(1).getId(), result.get(1).getId());
        assertEquals(requestResponseDtos.get(1).getDescription(), result.get(1).getDescription());
        assertEquals(requestResponseDtos.get(1).getCreated(), result.get(1).getCreated());

        verify(mockUserService, times(1)).findById(user.getId());
        verify(mockRequestRepository, times(1)).findAllByUserIdNot(eq(user.getId()), any(Pageable.class));
    }

    private void createTestObjects() {
        user = User.builder()
                .id(1L)
                .name("John Travolta")
                .email("travolta@hollywood.com")
                .build();

        userResponseDto = UserResponseDto.builder()
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

        requests = List.of(request1, request2);

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
    }
}
