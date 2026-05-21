package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getAll() {
        Collection<User> userList = users.values().stream().toList();
        log.info("Запрошен список всех пользователей. Количество элементов: {}", userList.size());
        return userList;
    }

    @Override
    public User getById(Long id) {
        User user = users.get(id);
        log.info("Запрошен пользователь с id {}.", id);
        if (user == null) throw new NotFoundException("Фильм с id " + id + " не найден");
        return user;
    }

    @Override
    public User create(User user) {
        user.setId(getUniqueId());
        setUserDefaultName(user);
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = getById(user.getId());
        setUserDefaultName(user);
        users.put(user.getId(), user);
        log.info("Пользователь {} обновлён. Новое значение: {}", oldUser, user);
        return user;
    }

    private void setUserDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
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
