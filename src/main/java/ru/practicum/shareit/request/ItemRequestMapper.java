package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return request != null ? ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build() : null;
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto) {
        return requestDto != null ? ItemRequest.builder()
                .id(requestDto.getId())
                .description(requestDto.getDescription())
                .created(requestDto.getCreated())
                .build() : null;
    }

    public static List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> requests) {
        return requests.stream().map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
    }
}
