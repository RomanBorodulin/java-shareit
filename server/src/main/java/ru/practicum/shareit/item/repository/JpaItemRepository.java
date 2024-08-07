package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface JpaItemRepository extends JpaRepository<Item, Long> {
    @Query("select i " +
            "from Item as i " +
            "where i.available = true and " +
            "(lower(i.name) like lower(concat('%', ?1, '%') ) or " +
            "lower(i.description) like lower(concat('%', ?1, '%') ))")
    List<Item> searchItems(String text, Pageable pageable);

    List<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    List<Item> findAllByRequestInOrderByIdAsc(List<ItemRequest> requests);

    List<Item> findAllByOwnerId(Long ownerId);
}
