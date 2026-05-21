package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    /* Вопрос 1:
    Насколько корректна ситуация, когда контроллер ходит напрямую в Storage?
    Исходя из текста задания, в сервисе должен быть реализован только функционал друзей и лайков,
    но доступ из контроллера в storage напрямую - это нечто вроде доступа сквозь один слой, не взаимодействую с ним.
    Как мне кажется, ситуация, когда контроллер общается только с сервисом, как и storage взаимодействует только с сервисом, сильно более предпочтительна
    Прав ли я?
     */

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }
}
