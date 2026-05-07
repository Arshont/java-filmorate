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

class FilmValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        // Инициализация валидатора (Hibernate Validator подтянется из classpath)
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A thief who steals corporate secrets through dream-sharing technology.");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    @Test
    void validFilm_shouldPass() {
        Set<ConstraintViolation<Film>> violations = validator.validate(createValidFilm());
        assertTrue(violations.isEmpty(), "Валидный объект не должен содержать ошибок");
    }

    @Test
    void blankName_shouldFail() {
        Film film = createValidFilm();
        film.setName("   ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFieldError(violations, FILM_NAME_PROPERTY, "Название не может быть пустым");
    }

    @Test
    void descriptionTooLong_shouldFail() {
        Film film = createValidFilm();
        film.setDescription("A".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFieldError(violations, FILM_DESCRIPTION_PROPERTY, "Длина описания не может превышать 200 символов");
    }

    @Test
    void nullReleaseDate_shouldFail() {
        Film film = createValidFilm();
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFieldError(violations, FILM_RELEASE_DATE_PROPERTY, "Дата выхода не должна быть пустой");
    }

    @Test
    void releaseDateTooEarly_shouldFail() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // На день раньше допустимого
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFieldError(violations, FILM_RELEASE_DATE_VALIDATION_PROPERTY, "Дата выхода не может быть раньше 28.12.1895");
    }

    @Test
    void zeroOrNegativeDuration_shouldFail() {
        Film film = createValidFilm();
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations1 = validator.validate(film);
        assertFieldError(violations1, FILM_DURATION_PROPERTY, "Продолжительность фильма должна быть положительной");

        film.setDuration(-10);
        Set<ConstraintViolation<Film>> violations2 = validator.validate(film);
        assertFieldError(violations2, FILM_DURATION_PROPERTY, "Продолжительность фильма должна быть положительной");
    }

    private void assertFieldError(Set<ConstraintViolation<Film>> violations, String fieldName, String expectedMessage) {
        assertFalse(violations.isEmpty(), "Ожидалась ошибка для поля: " + fieldName);

        ConstraintViolation<Film> error = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ошибка для поля '" + fieldName + "' не найдена"));

        assertEquals(expectedMessage, error.getMessage(), "Некорректное сообщение об ошибке");
    }
}