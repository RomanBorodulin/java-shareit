package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Override
    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        userValidator.validateAddUser(user);
        return UserMapper.toUserDto(userRepository.add(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User savedUser = userValidator.validateIfNotExist(userId);
        User user = UserMapper.toUser(userDto);
        userValidator.validateUpdateUser(userId, user);
        user.setId(savedUser.getId());
        if (user.getName() == null) {
            user.setName(savedUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(savedUser.getEmail());
        }
        return UserMapper.toUserDto(userRepository.update(userId, user));
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userValidator.validateIfNotExist(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        userValidator.validateIfNotExist(userId);
        userRepository.delete(userId);
    }

}
