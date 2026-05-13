package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.utils.TestConstants.*;

class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        // Инициализация валидатора (Hibernate Validator подтянется из classpath)
        validator = Validation.buildDefaultValidatorFactory().getValidator();
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
    void validUser_shouldPass() {
        Set<ConstraintViolation<User>> violations = validator.validate(createValidUser());
        assertTrue(violations.isEmpty(), "Валидный объект не должен содержать ошибок");
    }

    @Test
    void nullEmail_shouldFail() {
        User user = createValidUser();
        user.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFieldError(violations, USER_EMAIL_PROPERTY, "Email не может быть пустым");
    }

    @Test
    void blankEmail_shouldFail() {
        User user = createValidUser();
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFieldError(violations, USER_EMAIL_PROPERTY, "Email не может быть пустым");
    }

    @Test
    void invalidEmail_shouldFail() {
        User user = createValidUser();
        user.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFieldError(violations, USER_EMAIL_PROPERTY, "Email некорректен");
    }

    @Test
    void nullLogin_shouldFail() {
        User user = createValidUser();
        user.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFieldError(violations, USER_LOGIN_PROPERTY, "Логин не может быть пустым");
    }

    @Test
    void blankLogin_shouldFail() {
        User user = createValidUser();
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFieldError(violations, USER_LOGIN_PROPERTY, "Пробелы и другие пробельные символы в логине не допускаются");
    }

    @Test
    void loginWithSpaces_shouldFail() {
        User user = createValidUser();
        user.setLogin("user login");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFieldError(violations, USER_LOGIN_PROPERTY, "Пробелы и другие пробельные символы в логине не допускаются");
    }

    @Test
    void futureBirthday_shouldFail() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFieldError(violations, USER_BIRTHDAY_PROPERTY, "Дата рождения не может быть в будущем");
    }

    private void assertFieldError(Set<ConstraintViolation<User>> violations, String fieldName, String expectedMessage) {
        assertFalse(violations.isEmpty(), "Ожидалась ошибка для поля: " + fieldName);

        ConstraintViolation<User> error = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ошибка для поля '" + fieldName + "' не найдена"));

        assertEquals(expectedMessage, error.getMessage(), "Некорректное сообщение об ошибке");
    }
}