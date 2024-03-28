package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidator {
    private final UserRepository userRepository;

    public void validateAddUser(User user) {
        if (user == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
        validateUniqueEmail(user);
    }

    public User validateIfNotExist(Long userId) {
        User user = userRepository.getById(userId);
        if (user == null) {
            log.warn("Пользователь с id={} не существует", userId);
            throw new DataNotFoundException("Пользователь с указанным id=" + userId + " не был добавлен ранее");
        }
        return user;
    }

    public void validateUpdateUser(Long userId, User user) {
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

    private void validateUniqueEmail(User user) {
        Set<String> emails = userRepository.getAllUsers().stream().map(User::getEmail).collect(Collectors.toSet());
        if (emails.contains(user.getEmail())) {
            log.warn("Пользователь с таким email {} уже существует", user.getEmail());
            throw new DuplicateEmailException("Пользователь с таким email уже существует");
        }
    }

    private void validateUniqueEmail(Long userId, User user) {
        Set<String> emails = userRepository.getAllUsers().stream().map(User::getEmail).collect(Collectors.toSet());
        User savedUser = userRepository.getById(userId);
        if (savedUser != null) {
            emails.remove(savedUser.getEmail());
        }
        if (emails.contains(user.getEmail())) {
            log.warn("Пользователь с таким email {} уже существует", user.getEmail());
            throw new DuplicateEmailException("Пользователь с таким email уже существует");
        }
    }
}
