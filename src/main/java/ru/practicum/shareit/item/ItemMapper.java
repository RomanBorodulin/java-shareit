package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    private ItemMapper() {

    }

    public static ItemDto toItemDto(Item item) {
        return item != null ?
                ItemDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .request(item.getRequest())
                        .build() : null;
    }

    public static Item toItem(ItemDto itemDto) {
        return itemDto != null ?
                Item.builder()
                        .id(itemDto.getId())
                        .name(itemDto.getName())
                        .description(itemDto.getDescription())
                        .available(itemDto.getAvailable())
                        .request(itemDto.getRequest())
                        .build() : null;
    }
}
