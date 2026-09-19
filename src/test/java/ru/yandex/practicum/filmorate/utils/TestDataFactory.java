package ru.yandex.practicum.filmorate.utils;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User user(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    public static Film film(String name, int mpaId, int... genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание фильма " + name);
        film.setReleaseDate(LocalDate.of(2000, 5, 20));
        film.setDuration(120);
        film.setMpa(mpa(mpaId));
        film.setGenres(Arrays.stream(genreIds)
                .mapToObj(TestDataFactory::genre)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return film;
    }

    public static Mpa mpa(int id) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        return mpa;
    }

    public static Genre genre(int id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }
}
