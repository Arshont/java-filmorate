package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConflictException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAll() {
        Collection<User> userList = userStorage.getAll();
        log.info("Запрошен список всех пользователей. Количество элементов: {}", userList.size());
        return userList;
    }

    public User getById(Long id) {
        Optional<User> optionalUser = userStorage.getById(id);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + id + " не найден");
        log.info("Запрошен и получен пользователь с id {}.", id);
        return optionalUser.get();
    }

    public User create(User user) {
        setUserDefaultName(user);
        User returnUser = userStorage.create(user);
        log.info("Создан пользователь: {}", user);
        return returnUser;
    }

    public User update(User user) {
        Optional<User> optionalUser = userStorage.getById(user.getId());
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        setUserDefaultName(user);
        User returnUser = userStorage.update(user);
        log.info("Пользователь {} обновлён. Новое значение: {}", optionalUser.get(), user);
        return returnUser;
    }

    public void addFriend(Long userId, Long friendId) {
        Optional<User> optionalUser = userStorage.getById(userId);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        Optional<User> optionalFriend = userStorage.getById(friendId);
        if (optionalFriend.isEmpty()) throw new NotFoundException("Пользователь с id " + friendId + " не найден");

        if (optionalUser.get() == optionalFriend.get())
            throw new ConflictException("Пользователь и его друг имеют одинаковый Id: " + userId);

        userStorage.addFriend(userId, friendId);
        log.info("Пользователи с id {} и {} теперь друзья", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        Optional<User> optionalUser = userStorage.getById(userId);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        Optional<User> optionalFriend = userStorage.getById(friendId);
        if (optionalFriend.isEmpty()) throw new NotFoundException("Пользователь с id " + friendId + " не найден");

        if (optionalUser.get() == optionalFriend.get())
            throw new ConflictException("Пользователь и его друг имеют одинаковый Id: " + userId);

        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователи с id {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        Optional<User> optionalUser = userStorage.getById(userId);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        Collection<User> friends = userStorage.getFriends(userId);
        log.info("Запрошен список друзей пользователя с id {}. Количество элементов: {}", userId, friends.size());
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        Optional<User> optionalUser = userStorage.getById(userId);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        Optional<User> optionalFriend = userStorage.getById(friendId);
        if (optionalFriend.isEmpty()) throw new NotFoundException("Пользователь с id " + friendId + " не найден");

        if (optionalUser.get() == optionalFriend.get())
            throw new ConflictException("Пользователь и его друг имеют одинаковый Id: " + userId);

        Collection<User> commonFriends = userStorage.getCommonFriends(userId, friendId);
        log.info("Запрошен список общих друзей пользователей с id {} и {}. Количество элементов: {}", userId, friendId, commonFriends.size());
        return commonFriends;
    }

    private static void setUserDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
