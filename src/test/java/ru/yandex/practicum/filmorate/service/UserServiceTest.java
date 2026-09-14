package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private User createAndSaveUser(String login) {
        User user = createValidUser();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        return userService.create(user);
    }

    @Test
    void createUser_shouldSetNameToLoginIfNameIsNull() {
        User user = createValidUser();
        user.setName(null);

        User createdUser = userService.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(user.getLogin(), createdUser.getName());
    }

    @Test
    void createUser_shouldKeepNameIfNotNull() {
        User user = createValidUser();

        User createdUser = userService.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(user.getName(), createdUser.getName());
    }

    @Test
    void updateUser_shouldSetNameToLoginIfNameIsNull() {
        User user = createValidUser();
        User createdUser = userService.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("new@example.com");
        updateUser.setLogin("newlogin");
        updateUser.setName(null);
        updateUser.setBirthday(LocalDate.of(1995, 5, 5));

        User updatedUser = userService.update(updateUser);

        assertEquals("newlogin", updatedUser.getName());
    }

    @Test
    void updateUser_shouldKeepNameIfNotNull() {
        User user = createValidUser();
        User createdUser = userService.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("new@example.com");
        updateUser.setLogin("newlogin");
        updateUser.setName("New Name");
        updateUser.setBirthday(LocalDate.of(1995, 5, 5));

        User updatedUser = userService.update(updateUser);

        assertEquals("New Name", updatedUser.getName());
    }

    @Test
    void updateUser_shouldThrowNotFoundIfUserNotExists() {
        User user = createValidUser();
        user.setId(999L);

        assertThrows(NotFoundException.class, () -> userService.update(user));
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        User user1 = createValidUser();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");

        User user2 = createValidUser();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");

        userService.create(user1);
        userService.create(user2);

        Collection<User> users = userService.getAll();

        assertEquals(2, users.size());
    }

    @Test
    void createUser_shouldAssignUniqueId() {
        User user = createValidUser();

        User createdUser = userService.create(user);

        assertNotNull(createdUser.getId());
        assertTrue(createdUser.getId() > 0);
    }

    @Test
    void createMultipleUsers_shouldAssignDifferentIds() {
        User user1 = createValidUser();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");

        User user2 = createValidUser();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");

        User createdUser1 = userService.create(user1);
        User createdUser2 = userService.create(user2);

        assertNotEquals(createdUser1.getId(), createdUser2.getId());
    }

    @Test
    void updateUser_shouldNotChangeId() {
        User user = createValidUser();
        User createdUser = userService.create(user);

        Long originalId = createdUser.getId();

        User updateUser = new User();
        updateUser.setId(originalId);
        updateUser.setEmail("updated@example.com");
        updateUser.setLogin("updatedlogin");
        updateUser.setName("Updated Name");
        updateUser.setBirthday(LocalDate.of(1995, 5, 5));

        User updatedUser = userService.update(updateUser);

        assertEquals(originalId, updatedUser.getId());
    }

    @Test
    void getAll_shouldReturnEmptyCollectionWhenNoUsers() {
        Collection<User> users = userService.getAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void addFriend_shouldAddFriendToBothUsers() {
        User user = createAndSaveUser("user1");
        User friend = createAndSaveUser("user2");

        userService.addFriend(user.getId(), friend.getId());

        assertTrue(userService.getById(user.getId()).getFriends().contains(friend.getId()));
        assertTrue(userService.getById(friend.getId()).getFriends().contains(user.getId()));
    }

    @Test
    void addFriend_shouldNotDuplicateFriend() {
        User user = createAndSaveUser("user1");
        User friend = createAndSaveUser("user2");

        userService.addFriend(user.getId(), friend.getId());
        userService.addFriend(user.getId(), friend.getId());

        assertEquals(1, userService.getById(user.getId()).getFriends().size());
        assertEquals(1, userService.getById(friend.getId()).getFriends().size());
    }

    @Test
    void addFriend_shouldThrowNotFoundIfUserNotExists() {
        User friend = createAndSaveUser("user2");

        assertThrows(NotFoundException.class, () -> userService.addFriend(999L, friend.getId()));
    }

    @Test
    void addFriend_shouldThrowNotFoundIfFriendNotExists() {
        User user = createAndSaveUser("user1");

        assertThrows(NotFoundException.class, () -> userService.addFriend(user.getId(), 999L));
    }

    @Test
    void deleteFriend_shouldRemoveFriendFromBothUsers() {
        User user = createAndSaveUser("user1");
        User friend = createAndSaveUser("user2");
        userService.addFriend(user.getId(), friend.getId());

        userService.deleteFriend(user.getId(), friend.getId());

        assertTrue(userService.getById(user.getId()).getFriends().isEmpty());
        assertTrue(userService.getById(friend.getId()).getFriends().isEmpty());
    }

    @Test
    void deleteFriend_shouldDoNothingIfUsersAreNotFriends() {
        User user = createAndSaveUser("user1");
        User friend = createAndSaveUser("user2");

        assertDoesNotThrow(() -> userService.deleteFriend(user.getId(), friend.getId()));
        assertTrue(userService.getById(user.getId()).getFriends().isEmpty());
        assertTrue(userService.getById(friend.getId()).getFriends().isEmpty());
    }

    @Test
    void deleteFriend_shouldThrowNotFoundIfUserNotExists() {
        User friend = createAndSaveUser("user2");

        assertThrows(NotFoundException.class, () -> userService.deleteFriend(999L, friend.getId()));
    }

    @Test
    void deleteFriend_shouldThrowNotFoundIfFriendNotExists() {
        User user = createAndSaveUser("user1");

        assertThrows(NotFoundException.class, () -> userService.deleteFriend(user.getId(), 999L));
    }

    @Test
    void getFriends_shouldReturnAllFriends() {
        User user = createAndSaveUser("user1");
        User friend1 = createAndSaveUser("user2");
        User friend2 = createAndSaveUser("user3");

        userService.addFriend(user.getId(), friend1.getId());
        userService.addFriend(user.getId(), friend2.getId());

        Collection<User> friends = userService.getFriends(user.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.contains(friend1));
        assertTrue(friends.contains(friend2));
    }

    @Test
    void getFriends_shouldReturnEmptyCollectionIfUserHasNoFriends() {
        User user = createAndSaveUser("user1");

        Collection<User> friends = userService.getFriends(user.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void getFriends_shouldThrowNotFoundIfUserNotExists() {
        assertThrows(NotFoundException.class, () -> userService.getFriends(999L));
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = createAndSaveUser("user1");
        User user2 = createAndSaveUser("user2");
        User commonFriend = createAndSaveUser("common");
        User ownFriend = createAndSaveUser("own");

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());
        userService.addFriend(user1.getId(), ownFriend.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(commonFriend));
    }

    @Test
    void getCommonFriends_shouldReturnEmptyCollectionIfNoCommonFriends() {
        User user1 = createAndSaveUser("user1");
        User user2 = createAndSaveUser("user2");
        User friendOfUser1 = createAndSaveUser("user3");
        User friendOfUser2 = createAndSaveUser("user4");

        userService.addFriend(user1.getId(), friendOfUser1.getId());
        userService.addFriend(user2.getId(), friendOfUser2.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void getCommonFriends_shouldThrowNotFoundIfUserNotExists() {
        User user = createAndSaveUser("user1");

        assertThrows(NotFoundException.class, () -> userService.getCommonFriends(999L, user.getId()));
    }

    @Test
    void getCommonFriends_shouldThrowNotFoundIfOtherUserNotExists() {
        User user = createAndSaveUser("user1");

        assertThrows(NotFoundException.class, () -> userService.getCommonFriends(user.getId(), 999L));
    }
}