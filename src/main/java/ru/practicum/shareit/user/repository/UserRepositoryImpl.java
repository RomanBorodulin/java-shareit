package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public User add(User user) {
        user.setId(++id);
        users.put(id, user);
        log.debug("Новый пользователь c id={} добавлен", user.getId());
        return user;
    }

    @Override
    public User update(Long userId, User user) {
        users.put(userId, user);
        log.debug("Пользователь c id={} обновлен", user.getId());
        return user;
    }

    @Override
    public User getById(Long userId) {
        return users.get(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long userId) {
        log.debug("Пользователь c id={} удален", userId);
        users.remove(userId);
    }
}
