package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final ItemRequestService requestService;
    private final BookingService bookingService;
    private UserDto ownerDto;
    private UserDto requesterDto;
    private ItemDto itemDto;
    private ItemRequestDto itemRequestDto;
    private BookingRequestDto bookingDto;
    private BookingRequestDto bookingPastDto;
    private CommentRequestDto commentDto;
    private Long ownerId;
    private Long requestorId;
    private Long requestId;
    private Long itemId;
    private Long bookingId;
    private Long bookingPastId;
    private Long commentId;

    @BeforeEach
    public void setUp() {
        ownerId = 1L;
        requestorId = 2L;
        requestId = 1L;
        itemId = 1L;
        bookingId = 1L;
        commentId = 1L;
        bookingPastId = 2L;
        ownerDto = UserDto.builder()
                .name("name")
                .email("name@email.ru")
                .build();
        requesterDto = UserDto.builder()
                .name("requestor")
                .email("requestor@email.ru")
                .build();
        itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("sth useful")
                .build();
        itemDto = ItemDto.builder()
                .name("useful thing")
                .description("really useful")
                .available(true)
                .requestId(requestId)
                .build();
        bookingDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(5))
                .build();
        commentDto = CommentRequestDto.builder()
                .text("nice thing")
                .build();
        bookingPastDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(1))
                .end(LocalDateTime.now())
                .build();
    }

    @Test
    void findAllByUserId() {
        userService.add(ownerDto);
        userService.add(requesterDto);
        requestService.add(requestorId, itemRequestDto);
        itemService.add(ownerId, itemDto);
        bookingService.add(requestorId, bookingDto);
        bookingService.approve(ownerId, bookingId, true);
        bookingService.add(requestorId, bookingPastDto);
        bookingService.approve(ownerId, bookingPastId, true);
        itemService.addComment(requestorId, commentDto, itemId);

        List<ItemRequestDto> result = requestService.findAllByUserId(requestorId);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void findAll() {
        userService.add(ownerDto);
        userService.add(requesterDto);
        requestService.add(requestorId, itemRequestDto);
        itemService.add(ownerId, itemDto);
        bookingService.add(requestorId, bookingDto);
        bookingService.approve(ownerId, bookingId, true);
        bookingService.add(requestorId, bookingPastDto);
        bookingService.approve(ownerId, bookingPastId, true);
        itemService.addComment(requestorId, commentDto, itemId);

        List<ItemRequestDto> result = requestService.findAll(ownerId, 0, 10);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());

        assertThrows(ValidationException.class, () -> requestService.findAll(ownerId, -1, 10));
        assertThrows(ValidationException.class, () -> requestService.findAll(ownerId, 0, 0));
    }
}