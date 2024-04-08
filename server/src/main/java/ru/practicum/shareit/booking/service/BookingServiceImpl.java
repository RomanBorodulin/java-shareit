package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.exception.DataAlreadyExistException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;
import ru.practicum.shareit.utility.PageUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final JpaBookingRepository bookingRepository;
    private final JpaUserRepository userRepository;
    private final JpaItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto add(Long userId, BookingRequestDto bookingDto) {
        User user = validateIfUserNotExist(userId);
        Item item = validateIfItemNotExist(bookingDto.getItemId());
        validateAddBooking(bookingDto, user, item);
        Booking booking = BookingMapper.toBooking(bookingDto, user, item);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = validateIfBookingExist(bookingId);
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new DataAlreadyExistException("Бронирование уже имеет статус " + booking.getStatus());
        }
        if (!ownerId.equals(booking.getItem().getOwner().getId())) {
            throw new DataNotFoundException("Подтверждать запрос может только владелец");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto findBookingById(Long userId, Long bookingId) {
        Booking booking = validateIfBookingExist(bookingId);
        validateIfUserNotExist(userId);
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new DataNotFoundException("Пользователь не является автором бронирования или создателем вещи");
        }
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> findAllBookingByUserIdAndState(Long userId, String state, int from, int size) {
        validateIfUserNotExist(userId);
        Pageable pageable = PageUtils.getPageable(from, size, Sort.by(Sort.Direction.DESC, "start"));
        switch (BookingState.valueOf(state)) {
            case ALL:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByBookerId(userId, pageable));
            case PAST:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByBookerIdAndEndBefore(userId,
                                LocalDateTime.now(), pageable));

            case FUTURE:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByBookerIdAndStartAfter(userId,
                                LocalDateTime.now(), pageable));

            case CURRENT:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(userId,
                                LocalDateTime.now(), LocalDateTime.now(), pageable));

            case WAITING:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByBookerIdAndStatus(userId,
                                BookingStatus.WAITING, pageable));

            case REJECTED:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByBookerIdAndStatus(userId,
                                BookingStatus.REJECTED, pageable));

            default:
                throw new IllegalArgumentException("Неверный статус");

        }
    }

    @Override
    public List<BookingResponseDto> findAllBookingByOwnerIdAndState(Long ownerId, String state, int from, int size) {
        validateIfUserNotExist(ownerId);
        Pageable pageable = PageUtils.getPageable(from, size, Sort.by(Sort.Direction.DESC, "start"));
        if (itemRepository.findAllByOwnerId(ownerId).isEmpty()) {
            throw new DataNotFoundException("У владельца нет ни одной вещи");
        }
        switch (BookingState.valueOf(state)) {
            case ALL:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByItemOwnerId(ownerId, pageable));
            case PAST:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByItemOwnerIdAndEndBefore(ownerId,
                                LocalDateTime.now(), pageable));

            case FUTURE:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByItemOwnerIdAndStartAfter(ownerId,
                                LocalDateTime.now(), pageable));

            case CURRENT:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(ownerId,
                                LocalDateTime.now(), LocalDateTime.now(), pageable));

            case WAITING:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByItemOwnerIdAndStatus(ownerId,
                                BookingStatus.WAITING, pageable));

            case REJECTED:
                return BookingMapper.toBookingResponseDtoList(
                        bookingRepository.findAllByItemOwnerIdAndStatus(ownerId,
                                BookingStatus.REJECTED, pageable));

            default:
                throw new IllegalArgumentException("Неверный статус");

        }
    }

    private User validateIfUserNotExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));

    }

    private Item validateIfItemNotExist(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь не найдена"));

    }

    private Booking validateIfBookingExist(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Бронирование не найдено"));
    }

    private void validateAddBooking(BookingRequestDto bookingDto, User user, Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования");
        }
        if (user.getId().equals(item.getOwner().getId())) {
            throw new DataNotFoundException("Вещь не доступна для бронирования владельцем");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }
    }
}
