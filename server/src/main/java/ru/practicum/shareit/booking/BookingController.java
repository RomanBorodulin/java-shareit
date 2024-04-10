package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto add(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody BookingRequestDto bookingDto) {
        return bookingService.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable Long bookingId,
                                      @RequestParam boolean approved) {
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findBookingByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long bookingId) {
        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> findAllBookingByUserIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                   @RequestParam(defaultValue = "ALL") String state,
                                                                   @RequestParam(defaultValue = "0") int from,
                                                                   @RequestParam(defaultValue = "10") int size) {
        return bookingService.findAllBookingByUserIdAndState(userId, state, from, size);

    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findAllBookingByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                                    @RequestParam(defaultValue = "ALL") String state,
                                                                    @RequestParam(defaultValue = "0") int from,
                                                                    @RequestParam(defaultValue = "10") int size) {
        return bookingService.findAllBookingByOwnerIdAndState(ownerId, state, from, size);

    }


}
