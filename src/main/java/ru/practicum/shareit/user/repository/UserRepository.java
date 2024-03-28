package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User add(User user);

    User update(Long userId, User user);

    User getById(Long userId);

    List<User> getAllUsers();

    void delete(Long userId);
}
