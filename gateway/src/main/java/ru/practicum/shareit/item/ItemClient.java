package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ItemDto add(Long userId, ItemDto itemDto) {
        return post("", userId, itemDto, ItemDto.class);
    }

    public ItemDto update(Long userId, Long id, ItemDto itemDto) {
        return patch("/" + id, userId, itemDto, ItemDto.class);
    }

    public ItemWithBookingDto findById(Long userId, Long id) {
        return get("/" + id, userId, ItemWithBookingDto.class);
    }

    public List<ItemWithBookingDto> findAllItems(Long userId, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters, List.class);
    }

    public List<ItemDto> searchItems(Long userId, String text, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters, List.class);
    }

    public CommentResponseDto addComment(long userId, CommentRequestDto commentDto, long itemId) {
        return post("/" + itemId + "/comment", userId, commentDto, CommentResponseDto.class);
    }
}
