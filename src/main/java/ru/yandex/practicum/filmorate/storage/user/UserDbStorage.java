package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String SELECT_ALL =
            "SELECT id, email, login, name, birthday FROM users ORDER BY id";

    private static final String SELECT_BY_ID =
            "SELECT id, email, login, name, birthday FROM users WHERE id = ?";

    private static final String INSERT_USER =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_USER =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    // MERGE вместо INSERT, чтобы повторная заявка в друзья не падала по первичному ключу
    private static final String MERGE_FRIENDSHIP =
            "MERGE INTO friendships KEY (user_id, friend_id) VALUES (?, ?)";

    private static final String DELETE_FRIENDSHIP =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

    private static final String SELECT_FRIENDS =
            "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                    "FROM friendships f " +
                    "JOIN users u ON u.id = f.friend_id " +
                    "WHERE f.user_id = ? " +
                    "ORDER BY u.id";

    private static final String SELECT_COMMON_FRIENDS =
            "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                    "FROM friendships f1 " +
                    "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                    "JOIN users u ON u.id = f1.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ? " +
                    "ORDER BY u.id";

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query(SELECT_ALL, userRowMapper);
    }

    @Override
    public Optional<User> getById(Long id) {
        return jdbcTemplate.query(SELECT_BY_ID, userRowMapper, id).stream().findFirst();
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update(MERGE_FRIENDSHIP, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.update(DELETE_FRIENDSHIP, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        return jdbcTemplate.query(SELECT_FRIENDS, userRowMapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        return jdbcTemplate.query(SELECT_COMMON_FRIENDS, userRowMapper, userId, friendId);
    }
}
