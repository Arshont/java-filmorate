package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.utils.TestDataFactory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void create_shouldAssignIdAndSaveAllFields() {
        User created = userStorage.create(TestDataFactory.user("alice"));

        assertThat(created.getId()).isNotNull().isPositive();

        Optional<User> found = userStorage.getById(created.getId());
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(user).hasFieldOrPropertyWithValue("email", "alice@example.com");
                    assertThat(user).hasFieldOrPropertyWithValue("login", "alice");
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Имя alice");
                    assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void getById_shouldReturnEmptyForUnknownId() {
        assertThat(userStorage.getById(9999L)).isEmpty();
    }

    @Test
    void getAll_shouldReturnAllCreatedUsers() {
        userStorage.create(TestDataFactory.user("bob"));
        userStorage.create(TestDataFactory.user("carol"));

        Collection<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getLogin).containsExactly("bob", "carol");
    }

    @Test
    void update_shouldChangeStoredFields() {
        User created = userStorage.create(TestDataFactory.user("dave"));

        created.setEmail("new@example.com");
        created.setLogin("newlogin");
        created.setName("Новое имя");
        created.setBirthday(LocalDate.of(1985, 3, 15));
        userStorage.update(created);

        assertThat(userStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("email", "new@example.com");
                    assertThat(user).hasFieldOrPropertyWithValue("login", "newlogin");
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Новое имя");
                    assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1985, 3, 15));
                });
    }

    @Test
    void addFriend_shouldBeOneDirectional() {
        User user = userStorage.create(TestDataFactory.user("eve"));
        User friend = userStorage.create(TestDataFactory.user("frank"));

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void addFriend_shouldBeIdempotent() {
        User user = userStorage.create(TestDataFactory.user("grace"));
        User friend = userStorage.create(TestDataFactory.user("heidi"));

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId())).hasSize(1);
    }

    @Test
    void deleteFriend_shouldRemoveOnlyOneDirection() {
        User user = userStorage.create(TestDataFactory.user("ivan"));
        User friend = userStorage.create(TestDataFactory.user("judy"));
        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(friend.getId(), user.getId());

        userStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
        assertThat(userStorage.getFriends(friend.getId())).hasSize(1);
    }

    @Test
    void getFriends_shouldReturnEmptyListForUserWithoutFriends() {
        User user = userStorage.create(TestDataFactory.user("ken"));

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void getCommonFriends_shouldReturnIntersection() {
        User user1 = userStorage.create(TestDataFactory.user("leo"));
        User user2 = userStorage.create(TestDataFactory.user("mia"));
        User common = userStorage.create(TestDataFactory.user("nick"));
        User onlyFirst = userStorage.create(TestDataFactory.user("olga"));

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user1.getId(), onlyFirst.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        assertThat(userStorage.getCommonFriends(user1.getId(), user2.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    @Test
    void getCommonFriends_shouldReturnEmptyListWhenNoIntersection() {
        User user1 = userStorage.create(TestDataFactory.user("paul"));
        User user2 = userStorage.create(TestDataFactory.user("quinn"));
        User friend = userStorage.create(TestDataFactory.user("rita"));
        userStorage.addFriend(user1.getId(), friend.getId());

        assertThat(userStorage.getCommonFriends(user1.getId(), user2.getId())).isEmpty();
    }
}
