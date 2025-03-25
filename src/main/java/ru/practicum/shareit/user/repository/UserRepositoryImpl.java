package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {
    Map<Long, User> users = new HashMap<>();
    private static long userId = 1;

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public Optional<User> getUser(long userId) {
        User user = users.get(userId);
        if (user == null) {
            log.info("Пользователя с id {} не существует", userId);
        } else {
            log.info("Пользователь с id {} найден", userId);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public User addUser(User user) {
        user.setId(userId++);
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь с id {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(long userId, User user) {
        if (users.containsKey(userId)) {
            User updateUser = users.get(userId);
            if (user.getName() != null) {
                updateUser.setName(user.getName());
            }
            if (user.getEmail() != null) {
                updateUser.setEmail(user.getEmail());
            }
        }
        log.info("Пользователь с id {} обновлен", userId);
        return users.get(userId);
    }

    @Override
    public void deleteUser(long userId) {
        users.remove(userId);
        log.info("Пользователь с id {} удален", userId);
    }
}
