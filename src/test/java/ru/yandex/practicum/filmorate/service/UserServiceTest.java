package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
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
}