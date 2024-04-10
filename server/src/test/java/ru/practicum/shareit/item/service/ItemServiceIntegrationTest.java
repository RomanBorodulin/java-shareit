package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {

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
    public void createItem_whenOwnerFound_andRequestFound_thenSavedItem() {
        userService.add(ownerDto);
        userService.add(requesterDto);
        requestService.add(requestorId, itemRequestDto);

        ItemDto result = itemService.add(ownerId, itemDto);

        assertEquals(itemId, result.getId());
        assertEquals(true, result.getAvailable());
        assertEquals("useful thing", result.getName());
        assertEquals("really useful", result.getDescription());
        assertEquals(requestId, result.getRequestId());
    }

    @Test
    public void getItemById_whenOwnerFound_andWithoutComment_thenReturnedItemWithBooking() {
        userService.add(ownerDto);
        userService.add(requesterDto);
        requestService.add(requestorId, itemRequestDto);
        itemService.add(ownerId, itemDto);
        bookingService.add(requestorId, bookingDto);
        bookingService.approve(ownerId, bookingId, true);

        ItemWithBookingDto result = itemService.getById(ownerId, itemId);

        assertEquals(itemId, result.getId());
        assertEquals(true, result.getAvailable());
        assertEquals("useful thing", result.getName());
        assertEquals("really useful", result.getDescription());
        assertEquals(Collections.EMPTY_LIST, result.getComments());
        assertEquals(requestorId, result.getNextBooking().getBookerId());
        assertEquals(BookingStatus.APPROVED, result.getNextBooking().getStatus());
    }

    @Test
    public void getItemById_whenOwnerNotFound_andWithoutComment_thenReturnedItemWithoutBooking() {
        userService.add(ownerDto);
        userService.add(requesterDto);
        requestService.add(requestorId, itemRequestDto);
        itemService.add(ownerId, itemDto);
        bookingService.add(requestorId, bookingDto);
        bookingService.approve(ownerId, bookingId, true);

        ItemWithBookingDto result = itemService.getById(requestorId, itemId);

        assertEquals(itemId, result.getId());
        assertEquals(true, result.getAvailable());
        assertEquals("useful thing", result.getName());
        assertEquals("really useful", result.getDescription());
        assertEquals(Collections.EMPTY_LIST, result.getComments());
        assertNull(result.getNextBooking());
    }

    @Test
    public void getItemById_whenOwnerNotFound_andWithComment_thenReturnedItemWithoutBooking() {
        userService.add(ownerDto);
        userService.add(requesterDto);
        requestService.add(requestorId, itemRequestDto);
        itemService.add(ownerId, itemDto);
        bookingService.add(requestorId, bookingDto);
        bookingService.approve(ownerId, bookingId, true);
        bookingService.add(requestorId, bookingPastDto);
        bookingService.approve(ownerId, bookingPastId, true);
        itemService.addComment(requestorId, commentDto, itemId);

        ItemWithBookingDto result = itemService.getById(requestorId, itemId);

        assertEquals(itemId, result.getId());
        assertEquals(true, result.getAvailable());
        assertEquals("useful thing", result.getName());
        assertEquals("really useful", result.getDescription());
        assertNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());
        assertEquals(commentId, result.getComments().get(0).getId());
        assertEquals(commentId, result.getComments().get(0).getId());
        assertEquals("nice thing", result.getComments().get(0).getText());
        assertEquals("requestor", result.getComments().get(0).getAuthorName());
        assertNotNull(result.getComments().get(0).getCreated());
    }

    @Test
    public void getItemById_andWithComment_whenAddCommentNotBooker_thenExceptionThrown() {
        userService.add(ownerDto);
        userService.add(requesterDto);
        requestService.add(requestorId, itemRequestDto);
        itemService.add(ownerId, itemDto);
        bookingService.add(requestorId, bookingDto);
        bookingService.approve(ownerId, bookingId, true);
        bookingService.add(requestorId, bookingPastDto);
        bookingService.approve(ownerId, bookingPastId, true);

        assertThrows(ValidationException.class, () -> itemService.addComment(ownerId, commentDto, itemId));

    }


}
