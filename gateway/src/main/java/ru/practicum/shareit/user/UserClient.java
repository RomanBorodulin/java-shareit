package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

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

    public UserDto add(UserDto userDto) {
        return post("", userDto, UserDto.class);
    }

    public UserDto update(Long id, UserDto userDto) {
        return patch("/" + id, userDto, UserDto.class);
    }

    public UserDto findById(Long id) {
        return get("/" + id, UserDto.class);
    }

    public List<UserDto> findAll() {
        return get("", List.class);
    }

    public void delete(Long id) {
        delete("/" + id);
    }
}
