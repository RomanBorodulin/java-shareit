package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.exception.DataAlreadyExistException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private JpaBookingRepository bookingRepository;
    @Mock
    private JpaUserRepository userRepository;
    @Mock
    private JpaItemRepository itemRepository;

    @Test
    public void createBooking_whenUserFound_andItemFound_thenSavedBooking() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        Booking booking = Booking.builder()
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(24))
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
        BookingRequestDto bookingDto = BookingMapper.toBookingRequestDto(booking);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto actualBooking = bookingService.add(userId, bookingDto);

        assertEquals(bookingDto.getStart(), actualBooking.getStart());
        assertEquals(bookingDto.getEnd(), actualBooking.getEnd());
        assertEquals(BookingStatus.WAITING, actualBooking.getStatus());

    }

    @Test
    public void createBooking_whenUserNotFound_andItemNotFound_thenDataNotFoundExceptionThrown() {
        Long itemId = 0L;
        Long userId = 0L;
        User user = new User(userId, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        Booking booking = Booking.builder()
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(24))
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
        BookingRequestDto bookingDto = BookingMapper.toBookingRequestDto(booking);
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> bookingService.add(userId, bookingDto));

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        assertThrows(DataNotFoundException.class, () -> bookingService.add(5L, bookingDto));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    public void createBooking_whenBookingNotValid_thenExceptionThrown() {
        Long itemId = 0L;
        Long userId = 0L;
        Long ownerId = 999L;
        User user = new User(userId, "user", "user@user");
        User owner = new User(ownerId, "owner", "owner@email");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(false)
                .owner(owner)
                .build();
        Booking booking = Booking.builder()
                .booker(owner)
                .start(LocalDateTime.now().plusHours(24))
                .end(LocalDateTime.now())
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
        BookingRequestDto bookingDto = BookingMapper.toBookingRequestDto(booking);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(999L)).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () -> bookingService.add(ownerId, bookingDto));

        item.setAvailable(true);
        booking.setItem(item);
        BookingRequestDto bookingDto1 = BookingMapper.toBookingRequestDto(booking);

        assertThrows(DataNotFoundException.class, () -> bookingService.add(ownerId, bookingDto1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class, () -> bookingService.add(userId, bookingDto1));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    public void approve_whenBookingFound_thenUpdatedStatus() {
        Long itemId = 0L;
        Long userId = 0L;
        Long bookingId = 0L;
        User user = new User(userId, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(24))
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto activeBooking = bookingService.approve(999L, bookingId, false);

        assertEquals(BookingStatus.REJECTED, activeBooking.getStatus());
    }

    @Test
    public void approve_whenBookingNotValid_thenExceptionThrown() {
        Long itemId = 0L;
        Long userId = 0L;
        Long bookingId = 0L;
        User user = new User(userId, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(24))
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(DataAlreadyExistException.class, ()
                -> bookingService.approve(0L, bookingId, false));

        booking.setStatus(BookingStatus.WAITING);
        assertThrows(DataNotFoundException.class, ()
                -> bookingService.approve(0L, bookingId, false));

    }

    @Test
    public void findBookingById_whenBookingFound_thenReturnedBooking() {
        Long itemId = 0L;
        Long userId = 0L;
        Long bookingId = 0L;
        User user = new User(userId, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(24))
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingResponseDto expectedBooking = BookingMapper.toBookingResponseDto(booking);
        BookingResponseDto actualBooking = bookingService.findBookingById(userId, bookingId);

        assertEquals(expectedBooking.getStart(), actualBooking.getStart());
        assertEquals(expectedBooking.getEnd(), actualBooking.getEnd());
        assertEquals(expectedBooking.getStatus(), actualBooking.getStatus());
        assertEquals(expectedBooking.getStatus(), actualBooking.getStatus());
        assertEquals(expectedBooking.getBooker().getId(), actualBooking.getBooker().getId());
        assertEquals(expectedBooking.getItem().getId(), actualBooking.getItem().getId());
    }

    @Test
    public void findBookingById_whenBookingNotFound_thenDataNotFoundExceptionThrown() {
        Long itemId = 0L;
        Long userId = 0L;
        Long bookingId = 0L;
        User user = new User(userId, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> bookingService.findBookingById(userId, bookingId));
    }

    @Test
    public void findBookingById_whenBookingNotValid_thenDataNotFoundExceptionThrown() {
        Long itemId = 1L;
        Long userId = 1L;
        Long bookingId = 1L;
        User user = new User(0L, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(24))
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();
        assertThrows(DataNotFoundException.class, () -> bookingService.findBookingById(0L, bookingId));

    }

    @Test
    public void findBookingById_whenBookingValid_andUserNotBookerAndNotOwner_thenDataNotFoundExceptionThrown() {
        Long itemId = 0L;
        Long userId = 0L;
        Long bookingId = 0L;
        User user = new User(userId, "user", "user@user");
        Item item = Item.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .owner(new User(999L, "owner", "owner@email"))
                .build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(24))
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(DataNotFoundException.class, () -> bookingService.findBookingById(1L, bookingId));

    }

}

