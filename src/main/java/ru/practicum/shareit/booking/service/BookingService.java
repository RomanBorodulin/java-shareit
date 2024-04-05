package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto add(Long userId, BookingRequestDto bookingDto);

    BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingResponseDto findBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> findAllBookingByUserIdAndState(Long userId, String state, int from, int size);

    List<BookingResponseDto> findAllBookingByOwnerIdAndState(Long ownerId, String state, int from, int size);
}
