package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserValidator userValidator;

    @Override
    public ItemDto add(Long userId, ItemDto itemDto) {
        userValidator.validateIfNotExist(userId);
        Item item = ItemMapper.toItem(itemDto);
        validateAddItem(item);
        item.setOwnerId(userId);
        return ItemMapper.toItemDto(itemRepository.add(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userValidator.validateIfNotExist(userId);
        Item item = ItemMapper.toItem(itemDto);
        validateUpdateItem(userId, itemId, item);
        Item savedItem = itemRepository.getById(itemId);
        item.setId(savedItem.getId());
        item.setOwnerId(savedItem.getOwnerId());
        if (item.getName() == null) {
            item.setName(savedItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(savedItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(savedItem.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.update(itemId, item));
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        Item item = validateIfNotExist(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        userValidator.validateIfNotExist(userId);
        return itemRepository.getAllItems().stream().filter(item -> item.getOwnerId().equals(userId))
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(Long userId, String text) {
        return itemRepository.searchItems(text).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public void validateAddItem(Item item) {
        if (item == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
        if (!item.getAvailable()) {
            log.warn("Получен недоступный статус вещи при создании");
            throw new ValidationException("Передан объект с недоступным статусом при создании");
        }

    }

    public void validateUpdateItem(Long userId, Long itemId, Item item) {
        if (item == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
        if (item.getName() != null && item.getName().isBlank()) {
            throw new ValidationException("Передано пустое имя вещи");
        }
        if (item.getDescription() != null && item.getDescription().isBlank()) {
            throw new ValidationException("Передано пустое описание вещи");
        }
        Item savedItem = validateIfNotExist(itemId);
        if (!savedItem.getOwnerId().equals(userId)) {
            log.warn("userId={} не совпадает с ownerId={}", userId, savedItem.getOwnerId());
            throw new DataNotFoundException(String.format("Пользователь с id=%s не " +
                    "создавал вещь с id=%s", userId, itemId));
        }
    }

    public Item validateIfNotExist(Long itemId) {
        Item savedItem = itemRepository.getById(itemId);
        if (savedItem == null) {
            log.warn("Вещь с id={} не существует", itemId);
            throw new DataNotFoundException("Вещь с указанным id=" + itemId + " не была добавлена ранее");
        }
        return savedItem;
    }
}
