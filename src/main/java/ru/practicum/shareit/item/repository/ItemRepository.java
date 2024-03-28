package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item add(Item item);

    Item update(Long itemId, Item item);

    Item getById(Long itemId);

    List<Item> getAllItems();

    List<Item> searchItems(String text);
}
