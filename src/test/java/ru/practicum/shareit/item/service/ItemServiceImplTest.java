package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaCommentRepository;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.JpaItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private JpaItemRepository itemRepository;
    @Mock
    private JpaUserRepository userRepository;
    @Mock
    private JpaBookingRepository bookingRepository;
    @Mock
    private JpaCommentRepository commentRepository;
    @Mock
    private JpaItemRequestRepository requestRepository;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    @Test
    public void createItem_whenOwnerFound_andRequestNull_thenSavedItem() {
        Long itemId = 0L;
        Item expectedItem = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();
        when(itemRepository.save(any(Item.class))).thenReturn(expectedItem);
        when(userRepository.findById(0L)).thenReturn(Optional.of(new User()));
        ItemDto expectedItemDto = ItemMapper.toItemDto(expectedItem);
        ItemDto actualItem = itemService.add(itemId, expectedItemDto);

        assertEquals(expectedItemDto.getName(), actualItem.getName());
        assertEquals(expectedItemDto.getDescription(), actualItem.getDescription());
        assertEquals(expectedItemDto.getAvailable(), actualItem.getAvailable());
        assertNull(actualItem.getRequestId());
    }

    @Test
    public void createItem_whenOwnerFound_andRequestNotFound_thenNotSavedItem() {
        Long itemId = 0L;
        Long userId = 0L;
        ItemRequest request = new ItemRequest(1L, "request", new User(), LocalDateTime.now());
        Item itemToSave = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .request(request)
                .build();
        when(userRepository.findById(itemId)).thenReturn(Optional.of(new User()));
        when(requestRepository.findById(anyLong())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class,
                () -> itemService.add(userId, ItemMapper.toItemDto(itemToSave)));
        verify(itemRepository, never()).save(itemToSave);
    }

    @Test
    public void createItem_whenOwnerNotFound_thenNotSavedItem() {
        Long userId = 0L;
        Item itemToSave = new Item();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class,
                () -> itemService.add(userId, ItemMapper.toItemDto(itemToSave)));
        verify(itemRepository, never()).save(itemToSave);
    }

    @Test
    public void createItem_whenItemNotValid_thenNotSavedItem() {
        Long userId = 0L;
        Item itemToSave = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        assertThrows(ValidationException.class,
                () -> itemService.add(userId, ItemMapper.toItemDto(null)));
        assertThrows(ValidationException.class,
                () -> itemService.add(userId, ItemMapper.toItemDto(itemToSave)));
        verify(itemRepository, never()).save(itemToSave);
    }

    @Test
    public void updateItem_whenItemFound_thenUpdatedItem() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        Item oldItem = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(user)
                .build();
        Item newItem = Item.builder()
                .name("Дрель2")
                .description("Непростая дрель")
                .available(false)
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemDto actualItemDto = itemService.update(userId, itemId, ItemMapper.toItemDto(newItem));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals(newItem.getName(), savedItem.getName());
        assertEquals(newItem.getDescription(), savedItem.getDescription());
        assertEquals(newItem.getAvailable(), savedItem.getAvailable());
    }

    @Test
    public void updateOnlyItemName_whenItemFound_thenUpdatedItemName() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        Item oldItem = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(user)
                .build();
        Item newItem = Item.builder()
                .name("Дрель2")
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemDto actualItemDto = itemService.update(userId, itemId, ItemMapper.toItemDto(newItem));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals(newItem.getName(), savedItem.getName());
        assertEquals("Простая дрель", savedItem.getDescription());
        assertEquals(true, savedItem.getAvailable());
    }

    @Test
    public void updateOnlyItemDescription_whenItemFound_thenUpdatedItemDescription() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        Item oldItem = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(user)
                .build();
        Item newItem = Item.builder()
                .description("Непростая дрель")
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemDto actualItemDto = itemService.update(userId, itemId, ItemMapper.toItemDto(newItem));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals("Дрель", savedItem.getName());
        assertEquals(newItem.getDescription(), savedItem.getDescription());
        assertEquals(true, savedItem.getAvailable());
    }

    @Test
    public void updateOnlyItemavAilable_whenItemFound_thenUpdatedItemAvailable() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        Item oldItem = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(user)
                .build();
        Item newItem = Item.builder()
                .available(false)
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemDto actualItemDto = itemService.update(userId, itemId, ItemMapper.toItemDto(newItem));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals("Дрель", savedItem.getName());
        assertEquals("Простая дрель", savedItem.getDescription());
        assertEquals(newItem.getAvailable(), savedItem.getAvailable());
    }


    @Test
    public void updateItem_whenItemNotValid_thenUpdatedItem() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        Item oldItem = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(user)
                .build();
        Item newItem = Item.builder()
                .name("")
                .description("")
                .available(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> itemService.update(userId, itemId, ItemMapper.toItemDto(null)));
        assertThrows(ValidationException.class,
                () -> itemService.update(userId, itemId, ItemMapper.toItemDto(newItem)));

        newItem.setName("name");
        assertThrows(ValidationException.class,
                () -> itemService.update(userId, itemId, ItemMapper.toItemDto(newItem)));

        newItem.setDescription("desc");
        assertThrows(DataNotFoundException.class,
                () -> itemService.update(99L, itemId, ItemMapper.toItemDto(newItem)));


    }

    @Test
    public void getAllItems_whenOwnerFound_thenReturnedItemsList() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        User booker = new User(3L, "booker", "booker@Booker");
        Item item1 = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(user)
                .build();
        Item item2 = Item.builder()
                .id(2L)
                .name("Клей")
                .description("Супер клей")
                .available(true)
                .owner(user)
                .build();
        List<Item> items = List.of(item1, item2);
        Booking last = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now())
                .item(item1)
                .booker(booker)
                .build();
        Booking next = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(2))
                .item(item1)
                .booker(booker)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(userId, PageRequest.of(0, 10))).thenReturn(items);
        List<ItemWithBookingDto> expectedItems = List.of(ItemMapper.toItemWithBookingDto(item1, last, next),
                ItemMapper.toItemWithBookingDto(item2, null, null));
        expectedItems.forEach((item) -> item.setComments(Collections.emptyList()));
        when(bookingRepository.findAllByItemInAndStatusAndStartBeforeAndStartNotOrderByItemIdAscStartDesc(any(),
                any(BookingStatus.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(last));
        when(bookingRepository.findAllByItemInAndStatusAndStartAfterOrderByItemIdAscStartAsc(
                any(), any(BookingStatus.class), any(LocalDateTime.class))).thenReturn(List.of(next));


        List<ItemWithBookingDto> actualItems = itemService.getAllItems(userId, 0, 10);

        assertEquals(expectedItems.size(), actualItems.size());
        assertEquals(expectedItems.get(0).getName(), actualItems.get(0).getName());
        assertEquals(expectedItems.get(0).getDescription(), actualItems.get(0).getDescription());
        assertEquals(expectedItems.get(0).getAvailable(), actualItems.get(0).getAvailable());
        assertEquals(expectedItems.get(0).getLastBooking(), actualItems.get(0).getLastBooking());
        assertEquals(expectedItems.get(0).getNextBooking(), actualItems.get(0).getNextBooking());
        assertEquals(expectedItems.get(0).getComments(), actualItems.get(0).getComments());
    }

    @Test
    public void getAllItems_whenOwnerNotFound_thenDataNotFoundExceptionThrown() {

        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        User booker = new User(3L, "booker", "booker@Booker");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(DataNotFoundException.class, () -> itemService.getAllItems(userId, 0, 10));
    }

    @Test
    public void searchItems_whenTextNotBlank_thenSearchedItems() {
        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User())
                .build();
        String text = "Дрель";
        List<ItemDto> expectedItems = List.of(ItemMapper.toItemDto(item));
        when(itemRepository.searchItems(text, PageRequest.of(0, 10))).thenReturn(List.of(item));

        List<ItemDto> actualItems = itemService.searchItems(0L, text, 0, 10);

        assertEquals(expectedItems.size(), actualItems.size());
        assertEquals(expectedItems.get(0).getName(), actualItems.get(0).getName());
        assertEquals(expectedItems.get(0).getDescription(), actualItems.get(0).getDescription());
        assertEquals(expectedItems.get(0).getAvailable(), actualItems.get(0).getAvailable());
    }

    @Test
    public void searchItems_whenTextBlank_thenSearchedEmptyList() {
        String text = "";
        List<ItemDto> actualItems = itemService.searchItems(0L, text, 0, 10);

        assertEquals(0, actualItems.size());
    }

    @Test
    public void searchItems_whenFromAndSizeNotValid_thenExceptionThrown() {
        String text = "search";
        assertThrows(ValidationException.class, () -> itemService.searchItems(0L, text, -1, 10));
        assertThrows(ValidationException.class, () -> itemService.searchItems(0L, text, 0, 0));
    }

}
