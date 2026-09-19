package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public Collection<Genre> getAll() {
        Collection<Genre> genres = genreStorage.getAll();
        log.info("Запрошен список всех жанров. Получено элементов: {}", genres.size());
        return genres;
    }

    public Genre getById(int id) {
        Genre genre = genreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
        log.info("Запрошен и получен жанр с id {}.", id);
        return genre;
    }
}
