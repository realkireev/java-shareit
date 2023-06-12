package ru.practicum.shareit.user.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.BaseClient;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public UserClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> create(UserRequestDto userRequestDto) {
        return post("", userRequestDto);
    }

    public ResponseEntity<Object> update(UserRequestDto userRequestDto, long userId) {
        Map<String, Object> parameters = Map.of(
                "userId", userId
        );
        return patch("/{userId}", parameters, userRequestDto);
    }

    public ResponseEntity<Object> delete(long userId) {
        Map<String, Object> parameters = Map.of(
                "userId", userId
        );
        delete("/{userId}", parameters);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Object> findAll() {
        return get("");
    }

    public ResponseEntity<Object> findById(long userId) {
        Map<String, Object> parameters = Map.of(
                "userId", userId
        );
        return get("/{userId}", parameters);
    }
}
