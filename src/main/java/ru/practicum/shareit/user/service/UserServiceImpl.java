package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final JpaUserRepository userRepository;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        userValidator.validateAddUser(user);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
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
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userValidator.validateIfNotExist(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        userValidator.validateIfNotExist(userId);
        userRepository.deleteById(userId);
    }

}
