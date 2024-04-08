package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaBookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime before,
                                                             LocalDateTime after, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime before,
                                                                LocalDateTime after, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByItemInAndStatusAndStartBeforeAndStartNotOrderByItemIdAscStartDesc(List<Item> items,
                                                                                             BookingStatus status,
                                                                                             LocalDateTime before,
                                                                                             LocalDateTime now);

    List<Booking> findAllByItemInAndStatusAndStartAfterOrderByItemIdAscStartAsc(List<Item> items, BookingStatus status,
                                                                                LocalDateTime after);

    Optional<Booking> findFirstByBookerIdAndItemIdAndStatusAndEndBefore(Long userId, Long itemId,
                                                                        BookingStatus approved, LocalDateTime now);
}
