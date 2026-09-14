package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        if (user == null) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        User friend = users.get(friendId);
        if (friend == null) throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи с id {} и {} теперь друзья", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        if (user == null) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        User friend = users.get(friendId);
        if (friend == null) throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи с id {} и {} больше не друзья", userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        User user = users.get(userId);
        if (user == null) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        List<User> friends = user.getFriends().stream().map(users::get).toList();
        log.info("Запрошен список друзей пользователя с id {}. Количество элементов: {}", userId, friends.size());
        return friends;
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        User user = users.get(userId);
        if (user == null) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        User friend = users.get(friendId);
        if (friend == null) throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        List<User> commonFriends = user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .map(this::getById)
                .toList();
        log.info("Запрошен список общих друзей пользователей с id {} и {}. Количество элементов: {}", userId, friendId, commonFriends.size());
        return commonFriends;
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
