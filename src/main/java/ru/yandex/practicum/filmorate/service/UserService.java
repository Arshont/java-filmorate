package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getAll() {
        List<User> userList = users.values().stream().toList();
        log.info("Запрошен список всех пользователей. Количество элементов: {}", userList.size());
        return userList;
    }

    public User create(User user) {
        user.setId(getUniqueId());
        setUserDefaultName(user);
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    private void setUserDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public User update(User user) {
        User oldUser = users.get(user.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Пользователь {} обновлён. Новое значение: {}", oldUser, user);
        return user;
    }

    private Long getUniqueId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
