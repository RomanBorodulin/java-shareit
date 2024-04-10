package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class JpaItemRepositoryTest {

    @Autowired
    private JpaItemRepository itemRepository;
    @Autowired
    private JpaUserRepository userRepository;

    @BeforeEach
    public void addItems() {
        userRepository.save(new User(1L, "user", "user@user"));
        userRepository.save(new User(2L, "user2", "user2@user"));
        itemRepository.save(Item.builder()
                .id(1L)
                .name("item")
                .description("findMe")
                .available(true)
                .owner(new User(1L, "user", "user@user"))
                .build());

        itemRepository.save(Item.builder()
                .id(2L)
                .name("find")
                .description("findMe")
                .available(false)
                .owner(new User(2L, "user2", "user2@user"))
                .build());
    }

    @Test
    void searchItems() {
        List<Item> actualItems = itemRepository.searchItems("find", PageRequest.of(0, 10));

        assertEquals(1, actualItems.size());
        assertEquals("item", actualItems.get(0).getName());
    }
}