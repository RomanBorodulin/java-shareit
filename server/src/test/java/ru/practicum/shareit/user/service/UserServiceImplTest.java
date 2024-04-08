package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private JpaUserRepository userRepository;
    private User expectedUser;
    private UserDto expectedUserDto;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void setUp() {
        expectedUser = User.builder()
                .id(1L)
                .name("user")
                .email("user@user.com")
                .build();
        expectedUserDto = UserMapper.toUserDto(expectedUser);
    }

    @Test
    public void createUser_whenUserValid_thenSavedUser() {
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        UserDto actualUser = userService.add(expectedUserDto);

        assertEquals(expectedUserDto.getId(), actualUser.getId());
        assertEquals(expectedUserDto.getName(), actualUser.getName());
        assertEquals(expectedUserDto.getEmail(), actualUser.getEmail());
        verify(userRepository, times(1)).save(expectedUser);
    }

    @Test
    public void createUser_whenUserNotValid_thenNotSavedUser() {
        User userToSave = new User();
        doThrow(ValidationException.class)
                .when(userRepository).save(userToSave);

        assertThrows(ValidationException.class,
                () -> userService.add(UserMapper.toUserDto(null)));
        assertThrows(ValidationException.class,
                () -> userService.add(UserMapper.toUserDto(userToSave)));
        verify(userRepository, never()).save(expectedUser);
    }

    @Test
    public void updateUser_whenUserFound_thenUpdatedUser() {
        Long userId = 0L;
        User oldUser = User.builder()
                .name("old")
                .email("old@user.com")
                .build();
        User newUser = User.builder()
                .name("new")
                .email("new@user.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));

        UserDto actualUser = userService.update(userId, UserMapper.toUserDto(newUser));

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals(newUser.getName(), savedUser.getName());
        assertEquals(newUser.getEmail(), savedUser.getEmail());

    }

    @Test
    public void updateOnlyUserName_whenUserFound_thenUpdatedUserName() {
        Long userId = 0L;
        User oldUser = User.builder()
                .name("old")
                .email("old@user.com")
                .build();
        User newUser = User.builder()
                .name("new")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));

        UserDto actualUser = userService.update(userId, UserMapper.toUserDto(newUser));

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals(newUser.getName(), savedUser.getName());
        assertEquals("old@user.com", savedUser.getEmail());

    }

    @Test
    public void updateOnlyUserEmail_whenUserFound_thenUpdatedUserEmail() {
        Long userId = 0L;
        User oldUser = User.builder()
                .name("old")
                .email("old@user.com")
                .build();
        User newUser = User.builder()
                .email("new@user.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));

        UserDto actualUser = userService.update(userId, UserMapper.toUserDto(newUser));

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals("old", savedUser.getName());
        assertEquals(newUser.getEmail(), savedUser.getEmail());

    }

    @Test
    public void updateUser_whenUserNotValid_thenValidationExceptionThrown() {
        Long userId = 0L;
        User oldUser = User.builder()
                .name("old")
                .email("old@user.com")
                .build();
        User newUser = new User(userId, "", "");
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));

        assertThrows(ValidationException.class,
                () -> userService.update(userId, UserMapper.toUserDto(null)));
        assertThrows(ValidationException.class,
                () -> userService.update(userId, null));
        assertThrows(ValidationException.class,
                () -> userService.update(userId, UserMapper.toUserDto(newUser)));

        newUser.setName("new");
        assertThrows(ValidationException.class,
                () -> userService.update(userId, UserMapper.toUserDto(newUser)));

        newUser.setEmail("@pop");
        assertThrows(ValidationException.class,
                () -> userService.update(userId, UserMapper.toUserDto(newUser)));
        verify(userRepository, never()).save(newUser);
    }

    @Test
    public void updateUser_whenUserEmailDuplicate_thenDuplicateEmailException() {
        Long userId = 0L;
        User oldUser = User.builder()
                .name("old")
                .email("old@user.com")
                .build();
        User newUser = User.builder()
                .name("new")
                .email("new@user.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userRepository.findAll()).thenReturn(List.of(new User(99L, "duplicate", "new@user.com"),
                oldUser));

        assertThrows(DuplicateEmailException.class,
                () -> userService.update(userId, UserMapper.toUserDto(newUser)));
        verify(userRepository, never()).save(newUser);

    }

    @Test
    public void getUserById_whenUserFound_thenReturnedUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(expectedUser));

        UserDto actualUser = userService.getById(expectedUser.getId());

        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
    }

    @Test
    public void getUserById_whenUserNotFound_thenExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        Long userId = 0L;

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class, () ->
                userService.getById(userId));
    }

    @Test
    public void getAllUsers_whenUsersFound_thenReturnedList() {
        when(userRepository.findAll()).thenReturn(List.of(expectedUser));

        List<UserDto> actualUsers = userService.getAllUsers();

        assertEquals(List.of(expectedUserDto), actualUsers);
    }

    @Test
    public void deleteUser_whenUserFound_thenDeletedUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(expectedUser));

        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

}
