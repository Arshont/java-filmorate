package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

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
        checkMpaExists(film.getMpa());
        checkGenresExist(film.getGenres());
        Film returnFilm = filmStorage.create(film);
        log.info("Создан фильм: {}", returnFilm);
        return returnFilm;
    }

    public Film update(Film film) {
        Optional<Film> optionalFilm = filmStorage.getById(film.getId());
        if (optionalFilm.isEmpty()) throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        checkMpaExists(film.getMpa());
        checkGenresExist(film.getGenres());
        Film returnFilm = filmStorage.update(film);
        log.info("Фильм {} обновлён. Новое значение: {}", optionalFilm.get(), returnFilm);
        return returnFilm;
    }

    public void addLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь с Id {} поставил лайк фильму с Id {}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        filmStorage.deleteLike(filmId, userId);
        log.info("Пользователь с Id {} удалил лайк фильму с Id {}", userId, filmId);
    }

    public Collection<Film> getMostPopular(int count) {
        Collection<Film> mostPopularFilms = filmStorage.getMostPopular(count);
        log.info("Запрошено {} самых популярных фильмов. Получено элементов {}", count, mostPopularFilms.size());
        return mostPopularFilms;
    }

    private void checkFilmExists(Long filmId) {
        if (filmStorage.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    private void checkUserExists(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void checkMpaExists(Mpa mpa) {
        if (mpa == null) {
            return;
        }
        if (mpaStorage.getById(mpa.getId()).isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с id " + mpa.getId() + " не найден");
        }
    }

    private void checkGenresExist(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        Set<Integer> requestedIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Integer> existingIds = genreStorage.getByIds(requestedIds).stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        List<Integer> unknownIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!unknownIds.isEmpty()) {
            throw new NotFoundException("Жанры с id " + unknownIds + " не найдены");
        }
    }
}
