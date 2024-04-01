package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

public interface ItemService {
    ItemDto add(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemWithBookingDto getById(Long userId, Long itemId);

    List<ItemWithBookingDto> getAllItems(Long userId);

    List<ItemDto> searchItems(Long userId, String text);

    CommentResponseDto addComment(Long userId, CommentRequestDto commentDto, Long itemId);
}
