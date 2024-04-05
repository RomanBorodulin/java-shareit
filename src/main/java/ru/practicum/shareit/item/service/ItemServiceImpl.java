package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaCommentRepository;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.JpaItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final JpaItemRepository itemRepository;
    private final JpaUserRepository userRepository;
    private final JpaBookingRepository bookingRepository;
    private final JpaCommentRepository commentRepository;
    private final JpaItemRequestRepository requestRepository;


    @Override
    @Transactional
    public ItemDto add(Long userId, ItemDto itemDto) {
        User user = validateIfUserNotExist(userId);
        Item item = ItemMapper.toItem(itemDto);
        validateAddItem(item);
        item.setOwner(user);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            ItemRequest request = validateIfItemRequestNotExist(requestId);
            item.setRequest(request);
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        validateIfUserNotExist(userId);
        Item item = ItemMapper.toItem(itemDto);
        validateUpdateItem(userId, itemId, item);
        Item savedItem = itemRepository.findById(itemId).get();
        item.setId(savedItem.getId());
        item.setOwner(savedItem.getOwner());
        if (item.getName() == null) {
            item.setName(savedItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(savedItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(savedItem.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemWithBookingDto getById(Long userId, Long itemId) {
        Item item = validateIfNotExist(itemId);
        if (!userId.equals(item.getOwner().getId())) {
            return setComment(ItemMapper.toItemWithBookingDto(item, null, null));
        }
        return getItemWithBookingDtos(Collections.singletonList(item)).get(0);

    }

    @Override
    public List<ItemWithBookingDto> getAllItems(Long userId, int from, int size) {
        validateIfUserNotExist(userId);
        Pageable pageable = getPageable(from, size);
        List<Item> items = itemRepository.findAllByOwnerId(userId, pageable);
        if (items.isEmpty()) {
            throw new DataNotFoundException("Пользователь не является владельцем");
        }
        return getItemWithBookingDtos(items);
    }

    @Override
    public List<ItemDto> searchItems(Long userId, String text, int from, int size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        Pageable pageable = getPageable(from, size);
        return itemRepository.searchItems(text, pageable).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private Pageable getPageable(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Введен отрицательный индекс первого элемента");
        }
        if (size <= 0) {
            throw new ValidationException("Количество элементов для отображения не может быть меньше или равно нулю");
        }
        return PageRequest.of(from / size, size);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, CommentRequestDto commentDto, Long itemId) {
        Booking booking = bookingRepository.findFirstByBookerIdAndItemIdAndStatusAndEndBefore(userId,
                        itemId, BookingStatus.APPROVED, LocalDateTime.now())
                .orElseThrow(() -> new ValidationException("Пользователь не брал в аренду эту вещь " +
                        "или срок аренды еще не закончился"));
        Comment comment = CommentMapper.toComment(commentDto, booking.getBooker(), booking.getItem());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void validateAddItem(Item item) {
        if (item == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
        if (!item.getAvailable()) {
            log.warn("Получен недоступный статус вещи при создании");
            throw new ValidationException("Передан объект с недоступным статусом при создании");
        }

    }

    private void validateUpdateItem(Long userId, Long itemId, Item item) {
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
        if (!savedItem.getOwner().getId().equals(userId)) {
            log.warn("userId={} не совпадает с ownerId={}", userId, savedItem.getOwner());
            throw new DataNotFoundException(String.format("Пользователь с id=%s не " +
                    "создавал вещь с id=%s", userId, itemId));
        }
    }

    private User validateIfUserNotExist(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.warn("Пользователь с id={} не существует", userId);
            throw new DataNotFoundException("Пользователь с указанным id=" + userId + " не был добавлен ранее");
        }
        return user.get();
    }

    private Item validateIfNotExist(Long itemId) {
        Optional<Item> savedItem = itemRepository.findById(itemId);
        if (savedItem.isEmpty()) {
            log.warn("Вещь с id={} не существует", itemId);
            throw new DataNotFoundException("Вещь с указанным id=" + itemId + " не была добавлена ранее");
        }
        return savedItem.get();
    }

    private ItemRequest validateIfItemRequestNotExist(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Запрос не найден"));
    }

    private List<ItemWithBookingDto> getItemWithBookingDtos(List<Item> items) {
        List<ItemWithBookingDto> result = new ArrayList<>();
        List<Booking> lastBookings =
                bookingRepository.findAllByItemInAndStatusAndStartBeforeAndStartNotOrderByItemIdAscStartDesc(items,
                        BookingStatus.APPROVED, LocalDateTime.now(), LocalDateTime.now());
        List<Booking> nextBookings = bookingRepository.findAllByItemInAndStatusAndStartAfterOrderByItemIdAscStartAsc(
                items, BookingStatus.APPROVED, LocalDateTime.now());
        Map<Long, List<Booking>> itemIdToLastBookings = lastBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        Map<Long, List<Booking>> itemIdToNextBookings = nextBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        items.forEach(item -> {
            List<Booking> lasts = itemIdToLastBookings.get(item.getId());
            List<Booking> nexts = itemIdToNextBookings.get(item.getId());
            result.add(ItemMapper.toItemWithBookingDto(item, lasts != null && !lasts.isEmpty() ? lasts.get(0) : null,
                    nexts != null && !nexts.isEmpty() ? nexts.get(0) : null));
        });
        return result.stream().map(this::setComment).collect(Collectors.toList());
    }

    private ItemWithBookingDto setComment(ItemWithBookingDto itemDto) {
        List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId()).orElse(Collections.emptyList());
        itemDto.setComments(CommentMapper.toCommentDtoList(comments));
        return itemDto;
    }
}
