package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(Long userId, ItemRequestDto requestDto);

    List<ItemRequestDto> findAllByUserId(Long userId);

    List<ItemRequestDto> findAll(Long userId, int from, int size);

    ItemRequestDto findById(Long userId, Long id);
}
