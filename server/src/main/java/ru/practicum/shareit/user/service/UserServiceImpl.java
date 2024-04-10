package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final JpaUserRepository userRepository;

    @Override
    @Transactional
    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validateAddUser(user);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User savedUser = validateIfNotExist(userId);
        User user = UserMapper.toUser(userDto);
        validateUpdateUser(userId, user);
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
        User user = validateIfNotExist(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        validateIfNotExist(userId);
        userRepository.deleteById(userId);
    }

    public void validateAddUser(User user) {
        if (user == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
    }

    private User validateIfNotExist(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.warn("Пользователь с id={} не существует", userId);
            throw new DataNotFoundException("Пользователь с указанным id=" + userId + " не был добавлен ранее");
        }
        return user.get();
    }

    private void validateUpdateUser(Long userId, User user) {
        if (user == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
        if (user.getName() != null && user.getName().isBlank()) {
            throw new ValidationException("Передано пустое имя пользователя");
        }
        if (user.getEmail() != null && user.getEmail().isBlank()) {
            throw new ValidationException("Передан пустой email пользователя");
        }
        if (user.getEmail() != null) {
            if (!EmailValidator.getInstance().isValid(user.getEmail())) {
                throw new ValidationException("Передан некорректный email - " + user.getEmail());
            }
            validateUniqueEmail(userId, user);
        }
    }

    private void validateUniqueEmail(Long userId, User user) {
        Set<String> emails = userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toSet());
        Optional<User> savedUser = userRepository.findById(userId);
        savedUser.ifPresent(value -> emails.remove(value.getEmail()));
        if (emails.contains(user.getEmail())) {
            log.warn("Пользователь с таким email {} уже существует", user.getEmail());
            throw new DuplicateEmailException("Пользователь с таким email уже существует");
        }
    }

}
