package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> getAll();

    Optional<Genre> getById(int id);

    /**
     * Возвращает только те жанры из переданных идентификаторов, которые есть в базе.
     * Нужен, чтобы одним запросом проверить корректность жанров фильма.
     */
    Collection<Genre> getByIds(Collection<Integer> ids);
}
