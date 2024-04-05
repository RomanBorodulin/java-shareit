package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService requestService;


    @Test
    @SneakyThrows
    void add() {
        Long userId = 1L;
        Item item = Item.builder()
                .name("useful item")
                .description("useful")
                .build();
        List<Item> items = List.of(item);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Wanna an useful item")
                .created(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .items(ItemMapper.toItemDtoList(items))
                .build();
        String itemRequestString = objectMapper.writeValueAsString(itemRequestDto);
        when(requestService.add(userId, itemRequestDto))
                .thenReturn(itemRequestDto);

        String result = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.items.[0].name", is("useful item")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(itemRequestString, result);
        verify(requestService).add(userId, itemRequestDto);
    }

    @Test
    @SneakyThrows
    void findAllByUserId() {
        Long userId = 1L;
        Item item = Item.builder()
                .name("useful item")
                .description("useful")
                .build();
        List<Item> items = List.of(item);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Wanna an useful item")
                .created(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .items(ItemMapper.toItemDtoList(items))
                .build();
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        String expectedItemRequestString = objectMapper.writeValueAsString(requests);
        when(requestService.findAllByUserId(userId)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items.[0].name", is("useful item")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(expectedItemRequestString, result);
        verify(requestService).findAllByUserId(userId);
    }

    @Test
    @SneakyThrows
    void findAll() {
        Long userId = 1L;
        Item item = Item.builder()
                .name("useful item")
                .description("useful")
                .build();
        List<Item> items = List.of(item);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Wanna an useful item")
                .created(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .items(ItemMapper.toItemDtoList(items))
                .build();
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        String expectedItemRequestString = objectMapper.writeValueAsString(requests);
        when(requestService.findAll(userId, 0, 10)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items.[0].name", is("useful item")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(expectedItemRequestString, result);
        verify(requestService).findAll(userId, 0, 10);
    }

    @Test
    @SneakyThrows
    void findById() {
        Long userId = 1L;
        Long requestId = 1L;
        Item item = Item.builder()
                .name("useful item")
                .description("useful")
                .build();
        List<Item> items = List.of(item);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Wanna an useful item")
                .created(LocalDateTime.of(2026, Month.APRIL, 12, 12, 0, 1))
                .items(ItemMapper.toItemDtoList(items))
                .build();
        String expectedItemRequestString = objectMapper.writeValueAsString(itemRequestDto);
        when(requestService.findById(userId, requestId))
                .thenReturn(itemRequestDto);

        String result = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedItemRequestString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(expectedItemRequestString, result);
        verify(requestService).findById(userId, requestId);


    }
}