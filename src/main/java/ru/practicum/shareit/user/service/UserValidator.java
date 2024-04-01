package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidator {
    private final JpaUserRepository userRepository;

    public void validateAddUser(User user) {
        if (user == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
    }

    public User validateIfNotExist(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.warn("Пользователь с id={} не существует", userId);
            throw new DataNotFoundException("Пользователь с указанным id=" + userId + " не был добавлен ранее");
        }
        return user.get();
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
