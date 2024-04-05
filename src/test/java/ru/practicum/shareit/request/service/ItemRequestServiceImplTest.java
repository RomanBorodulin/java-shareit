package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.JpaItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private JpaItemRequestRepository requestRepository;
    @Mock
    private JpaUserRepository userRepository;
    @Mock
    private JpaItemRepository itemRepository;

    @Test
    public void createRequest_whenRequestorFound_thenSavedRequest() {
        Long userId = 0L;
        Long requestId = 0L;
        User user = new User(userId, "user", "user@user");
        ItemRequest request = ItemRequest.builder()
                .id(requestId)
                .description("description")
                .created(LocalDateTime.of(2024, Month.APRIL, 4, 12, 0, 0))
                .requestor(user)
                .build();
        ItemRequestDto expectedRequest = ItemRequestMapper.toItemRequestDto(request);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto actualRequest = itemRequestService.add(userId, expectedRequest);

        assertEquals(expectedRequest.getDescription(), actualRequest.getDescription());
        assertEquals(expectedRequest.getCreated(), actualRequest.getCreated());

    }

    @Test
    public void createRequest_whenRequestorNotFound_thenDataNotFoundExceptionThrown() {
        Long userId = 0L;
        Long requestId = 0L;
        User user = new User(userId, "user", "user@user");
        ItemRequest request = ItemRequest.builder()
                .id(requestId)
                .description("description")
                .created(LocalDateTime.of(2024, Month.APRIL, 4, 12, 0, 0))
                .requestor(new User(99L, "not", "not@hot"))
                .build();
        ItemRequestDto expectedRequest = ItemRequestMapper.toItemRequestDto(request);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> itemRequestService.add(99L, expectedRequest));
        verify(requestRepository, never()).save(request);

    }

    @Test
    public void findById_whenRequestFound_thenReturnedRequest() {
        Long userId = 0L;
        Long requestId = 0L;
        User user = new User(userId, "user", "user@user");
        ItemRequest request = ItemRequest.builder()
                .id(requestId)
                .description("description")
                .created(LocalDateTime.of(2024, Month.APRIL, 4, 12, 0, 0))
                .requestor(user)
                .build();
        ItemRequestDto expectedRequest = ItemRequestMapper.toItemRequestDto(request);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        ItemRequestDto actualRequest = itemRequestService.findById(userId, requestId);

        assertEquals(expectedRequest.getDescription(), actualRequest.getDescription());
        assertEquals(expectedRequest.getCreated(), actualRequest.getCreated());

    }

    @Test
    public void findById_whenRequestNotFound_thenReturnedRequest() {
        Long userId = 0L;
        Long requestId = 0L;
        User user = new User(userId, "user", "user@user");
        ItemRequest request = ItemRequest.builder()
                .id(requestId)
                .description("description")
                .created(LocalDateTime.of(2024, Month.APRIL, 4, 12, 0, 0))
                .requestor(user)
                .build();
        ItemRequestDto expectedRequest = ItemRequestMapper.toItemRequestDto(request);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> itemRequestService.findById(userId, requestId));

    }
}
