package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;

    @Test
    @SneakyThrows
    public void create_whenItemIsNotValid_thenReturnedBadRequest() {
        Long userId = 1L;
        String emptyName = "";
        ItemDto itemToAdd = ItemDto.builder()
                .id(userId)
                .name(emptyName)
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        String result = mockMvc.perform(post("/items/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToAdd)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, never()).add(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void update() {
        Long userId = 1L;
        Long itemId = 1L;
        Long requestId = 1L;
        ItemDto expectedItem = ItemDto.builder()
                .id(itemId)
                .name("item")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        when(itemService.update(userId, itemId, expectedItem))
                .thenReturn(expectedItem);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedItem)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).update(userId, itemId, expectedItem);

        assertEquals(objectMapper.writeValueAsString(expectedItem), result);
    }

    @Test
    @SneakyThrows
    void getById() {
        Long itemId = 1L;
        Long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Item")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();
        Item item = ItemMapper.toItem(itemDto);
        ItemWithBookingDto expectedItem = ItemMapper.toItemWithBookingDto(item, null, null);
        String expectedItemString = objectMapper.writeValueAsString(expectedItem);
        when(itemService.getById(userId, itemId))
                .thenReturn(expectedItem);

        String result = mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedItemString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).getById(userId, itemId);

        assertEquals(expectedItemString, result);
    }

    @Test
    @SneakyThrows
    void getAllItems() {
        int from = 0;
        int size = 10;
        Long itemId = 1L;
        Long userId = 1L;
        Long requestId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("item")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        Item item = ItemMapper.toItem(itemDto);
        List<ItemWithBookingDto> expectedItems = List.of(ItemMapper.toItemWithBookingDto(item, null, null));
        when(itemService.getAllItems(userId, from, size)).thenReturn(expectedItems);

        String result = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItems)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).getAllItems(userId, from, size);

        assertEquals(objectMapper.writeValueAsString(expectedItems), result);

    }

    @Test
    @SneakyThrows
    void searchItems() {
        int from = 0;
        int size = 10;
        Long itemId = 1L;
        Long requestId = 1L;
        Long userId = 1L;

        ItemDto itemDto1 = ItemDto.builder()
                .id(itemId)
                .name("need")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto itemDto2 = ItemDto.builder()
                .id(itemId)
                .name("should")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        List<ItemDto> items = List.of(itemDto1, itemDto2);
        String itemsString = objectMapper.writeValueAsString(items);
        when(itemService.searchItems(userId, "desc", from, size))
                .thenReturn(items);

        String result = mockMvc.perform(get("/items/search")
                        .param("text", "desc").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(itemsString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).searchItems(userId, "desc", from, size);

        assertEquals(result, itemsString);
    }

    @Test
    @SneakyThrows
    void addComment_whenCommentNotValid_thenReturnedBAdRequest() {
        Long commentId = 1L;
        Long userId = 1L;
        Long itemId = 1L;
        CommentRequestDto request = CommentRequestDto.builder()
                .text(null)
                .build();
        Comment comment = CommentMapper.toComment(request, new User(), new Item());
        CommentResponseDto responseDto = CommentMapper.toCommentDto(comment);
        when(itemService.addComment(userId, request, itemId))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).addComment(anyLong(), any(CommentRequestDto.class), anyLong());
    }
}