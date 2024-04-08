package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.JpaItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;
import ru.practicum.shareit.utility.PageUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final JpaItemRequestRepository requestRepository;
    private final JpaUserRepository userRepository;
    private final JpaItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto add(Long userId, ItemRequestDto requestDto) {
        User user = validateIfUserNotExist(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);
        return ItemRequestMapper.toItemRequestDto(requestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> findAllByUserId(Long userId) {
        User user = validateIfUserNotExist(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorOrderByCreatedDesc(user);
        List<ItemRequestDto> requestDtos = ItemRequestMapper.toItemRequestDtoList(requests);
        List<ItemDto> items = ItemMapper.toItemDtoList(itemRepository.findAllByRequestInOrderByIdAsc(requests));
        return getItemRequestDtosWithItems(requestDtos, items);
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId, int from, int size) {
        User user = validateIfUserNotExist(userId);
        Pageable pageable = PageUtils.getPageable(from, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> requests = requestRepository.findAllByRequestorNot(user, pageable);
        List<ItemRequestDto> requestDtos = ItemRequestMapper.toItemRequestDtoList(requests);
        List<ItemDto> items = ItemMapper.toItemDtoList(itemRepository.findAllByRequestInOrderByIdAsc(requests));
        return getItemRequestDtosWithItems(requestDtos, items);
    }

    @Override
    public ItemRequestDto findById(Long userId, Long id) {
        User user = validateIfUserNotExist(userId);
        ItemRequest request = validateIfItemRequestNotExist(id);
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(request);
        List<ItemDto> items = ItemMapper.toItemDtoList(
                itemRepository.findAllByRequestInOrderByIdAsc(Collections.singletonList(request)));
        requestDto.setItems(items);
        return requestDto;
    }

    private User validateIfUserNotExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));

    }

    private ItemRequest validateIfItemRequestNotExist(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Запрос не найден"));
    }

    private static List<ItemRequestDto> getItemRequestDtosWithItems(List<ItemRequestDto> requests,
                                                                    List<ItemDto> items) {
        Map<Long, List<ItemDto>> requestIdToItems = items.stream()
                .collect(Collectors.groupingBy(ItemDto::getRequestId));
        requests.forEach(request ->
                request.setItems(requestIdToItems.getOrDefault(request.getId(), Collections.emptyList())));
        return requests;
    }
}
