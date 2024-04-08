package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingResponseShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    private BookingMapper() {
    }

    public static BookingRequestDto toBookingRequestDto(Booking booking) {
        return booking != null ?
                BookingRequestDto.builder()
                        .itemId(booking.getItem().getId())
                        .start(booking.getStart())
                        .end(booking.getEnd())
                        .build() : null;
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        return booking != null ?
                BookingResponseDto.builder()
                        .id(booking.getId())
                        .start(booking.getStart())
                        .end(booking.getEnd())
                        .status(booking.getStatus())
                        .booker(UserDto.builder().id(booking.getBooker().getId()).build())
                        .item(ItemDto.builder()
                                .id(booking.getItem().getId())
                                .name(booking.getItem().getName())
                                .build())
                        .build() : null;
    }

    public static BookingResponseShortDto toBookingResponseShortDto(Booking booking) {
        return booking != null ?
                BookingResponseShortDto.builder()
                        .id(booking.getId())
                        .start(booking.getStart())
                        .end(booking.getEnd())
                        .status(booking.getStatus())
                        .bookerId(booking.getBooker().getId())
                        .build() : null;
    }

    public static Booking toBooking(BookingRequestDto bookingDto, User user, Item item) {
        return bookingDto != null ?
                Booking.builder()
                        .start(bookingDto.getStart())
                        .end(bookingDto.getEnd())
                        .item(item)
                        .booker(user)
                        .build() : null;
    }

    public static List<BookingResponseDto> toBookingResponseDtoList(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}
