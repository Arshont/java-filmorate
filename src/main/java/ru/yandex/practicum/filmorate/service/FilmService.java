package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getAll() {
        Collection<Film> filmList = filmStorage.getAll();
        log.info("Запрошен список всех фильмов. Получено элементов: {}", filmList.size());
        return filmList;
    }

    public Film getById(Long id) {
        Optional<Film> optionalFilm = filmStorage.getById(id);
        if (optionalFilm.isEmpty()) throw new NotFoundException("Фильм с id " + id + " не найден");
        log.info("Запрошен и получен фильм с id {}.", id);
        return optionalFilm.get();
    }

    public Film create(Film film) {
        Film returnFilm = filmStorage.create(film);
        log.info("Создан фильм: {}", film);
        return returnFilm;
    }

    public Film update(Film film) {
        Optional<Film> optionalFilm = filmStorage.getById(film.getId());
        if (optionalFilm.isEmpty()) throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        Film returnFilm = filmStorage.update(film);
        log.info("Фильм {} обновлён. Новое значение: {}", optionalFilm.get(), returnFilm);
        return returnFilm;
    }

    public void addLike(Long filmId, Long userId) {
        Optional<Film> optionalFilm = filmStorage.getById(filmId);
        if (optionalFilm.isEmpty()) throw new NotFoundException("Фильм с id " + filmId + " не найден");
        Optional<User> optionalUser = userStorage.getById(userId);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + userId + " не найден");

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь с Id {} поставил лайк фильму с Id {}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Optional<Film> optionalFilm = filmStorage.getById(filmId);
        if (optionalFilm.isEmpty()) throw new NotFoundException("Фильм с id " + filmId + " не найден");
        Optional<User> optionalUser = userStorage.getById(userId);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id " + userId + " не найден");

        filmStorage.deleteLike(filmId, userId);
        log.info("Пользователь с Id {} удалил лайк фильму с Id {}", userId, filmId);
    }

    public Collection<Film> getMostPopular(int count) {
        Collection<Film> mostPopularFilms = filmStorage.getMostPopular(count);
        log.info("Запрошено {} самых популярных фильмов. Получено элементов {}", count, mostPopularFilms.size());
        return mostPopularFilms;
    }
}
