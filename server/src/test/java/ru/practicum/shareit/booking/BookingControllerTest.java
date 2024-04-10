package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    @Test
    @SneakyThrows
    void add() {
        User user = User.builder().id(1L).name("name").email("email@name").build();
        Item item = Item.builder()
                .id(1L).name("item").description("descr").available(true)
                .build();
        BookingRequestDto bookingDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(4))
                .build();
        BookingResponseDto expectedDto = BookingMapper.toBookingResponseDto(
                BookingMapper.toBooking(bookingDto, user, item));
        when(bookingService.add(1L, bookingDto)).thenReturn(expectedDto);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(expectedDto.getStatus()), BookingStatus.class))
                .andExpect(jsonPath("$.booker.id", is(expectedDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(expectedDto.getItem().getId()), Long.class));

        verify(bookingService, times(1)).add(1L, bookingDto);
    }

    @Test
    @SneakyThrows
    void approve() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("name@email")
                .build();
        Item item = Item.builder()
                .name("item")
                .build();
        Long bookingId = 1L;
        BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
                .start(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .end(LocalDateTime.of(2026, Month.APRIL, 13, 13, 0, 2))
                .booker(UserMapper.toUserDto(user))
                .item(ItemMapper.toItemDto(item))
                .build();
        String expectedBookingString = objectMapper.writeValueAsString(bookingResponseDto);
        when(bookingService.approve(1L, bookingId, true))
                .thenReturn(bookingResponseDto);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("approved", "true")
                        .content(expectedBookingString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus())))
                .andExpect(jsonPath("$.item.name", is("item")))
                .andExpect(jsonPath("$.booker.name", is("name")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(expectedBookingString, result);
        verify(bookingService, times(1)).approve(1L, bookingId, true);
    }

    @Test
    @SneakyThrows
    void findBookingByUserId() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("name@email")
                .build();
        Item item = Item.builder()
                .name("item")
                .build();
        Long userId = 1L;
        Long bookingId = 1L;
        BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
                .start(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .end(LocalDateTime.of(2026, Month.APRIL, 13, 13, 0, 2))
                .booker(UserMapper.toUserDto(user))
                .item(ItemMapper.toItemDto(item))
                .build();
        String expectedBookingString = objectMapper.writeValueAsString(bookingResponseDto);

        when(bookingService.findBookingById(bookingId, userId))
                .thenReturn(bookingResponseDto);

        String result = mockMvc.perform(get("/bookings/{id}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedBookingString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(expectedBookingString, result);
        verify(bookingService, times(1)).findBookingById(bookingId, userId);
    }

    @Test
    @SneakyThrows
    void findAllBookingByUserIdAndState_whenStatusIsUnsupported_thenBadRequest() {
        BookingResponseDto bookingDto1 = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .end(LocalDateTime.of(2026, Month.APRIL, 13, 13, 0, 2))
                .status(BookingStatus.APPROVED)
                .build();
        BookingResponseDto bookingDto2 = BookingResponseDto.builder()
                .id(2L)
                .start(bookingDto1.getStart().plusHours(3))
                .end(bookingDto1.getEnd().plusHours(4))
                .status(BookingStatus.WAITING)
                .build();
        List<BookingResponseDto> bookingResponseDtoList = List.of(bookingDto1, bookingDto2);
        String expectedBookingsListString = objectMapper.writeValueAsString(bookingResponseDtoList);
        when(bookingService.findAllBookingByUserIdAndState(1L, "APPROVED", 0, 10))
                .thenReturn(bookingResponseDtoList);

        String result = mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "APPROVED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingDto1.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDto1.getEnd().toString())))
                .andExpect(jsonPath("$.[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$.[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].start", is(bookingDto2.getStart().toString())))
                .andExpect(jsonPath("$.[1].end", is(bookingDto2.getEnd().toString())))
                .andExpect(jsonPath("$.[1].status", is(bookingDto2.getStatus().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(expectedBookingsListString, result);
        verify(bookingService).findAllBookingByUserIdAndState(1L, "APPROVED", 0, 10);
    }

    @Test
    @SneakyThrows
    void findAllBookingByOwnerIdAndState() {
        BookingResponseDto bookingDto1 = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .end(LocalDateTime.of(2026, Month.APRIL, 13, 13, 0, 2))
                .status(BookingStatus.APPROVED)
                .build();
        BookingResponseDto bookingDto2 = BookingResponseDto.builder()
                .id(2L)
                .start(bookingDto1.getStart().plusHours(3))
                .end(bookingDto1.getEnd().plusHours(4))
                .status(BookingStatus.WAITING)
                .build();
        List<BookingResponseDto> bookingResponseDtoList = List.of(bookingDto1, bookingDto2);
        String expectedBookingsListString = objectMapper.writeValueAsString(bookingResponseDtoList);
        when(bookingService.findAllBookingByOwnerIdAndState(1L, "APPROVED", 0, 10))
                .thenReturn(bookingResponseDtoList);

        String result = mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "APPROVED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingDto1.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDto1.getEnd().toString())))
                .andExpect(jsonPath("$.[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$.[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].start", is(bookingDto2.getStart().toString())))
                .andExpect(jsonPath("$.[1].end", is(bookingDto2.getEnd().toString())))
                .andExpect(jsonPath("$.[1].status", is(bookingDto2.getStatus().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(expectedBookingsListString, result);
        verify(bookingService).findAllBookingByOwnerIdAndState(1L, "APPROVED", 0, 10);
    }
}