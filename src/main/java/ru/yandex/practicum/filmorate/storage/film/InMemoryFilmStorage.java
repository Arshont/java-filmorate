package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    private static final Comparator<Film> BY_LIKES_DESC =
            Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed();
    /* Вопрос 1:
     * Где логировать CRUD операции? В сервисе или в Storage?
     * Или и там, и там, просто с разным контекстом (Если так, то какую дополнительную информацию нужно фиксировать)?
     *
     * Вопрос 2:
     * Где правильнее выбрасывать NotFoundException?
     * В методе getById и любой запрос значения из БД(сейчас просто мапы) проводить исключительно через этот метод?
     * Или же выбрасывать NotFound при проверке Optional значения, возвращенного из getById, на null?
     * Сейчас реализован первый вариант, поскольку он кажется мне более лаконичным, а следовательно более понятным и надёжным
     * Возможно, есть и ещё варианты, которые я не рассмотрел выше
     *
     * Вопрос 3:
     * Реализация первого решения из прошлого вопроса выглядит костыльно, ввиду неиспользования результата выполнения метода.
     * Хотя, конечно, и до него этого использования фактически не было, просто теперь это предупреждение от IDE
     * Итого, я предельно озадачен корректной организацией данного момента и запрашиваю обратную связь*/

    @Override
    public Collection<Film> getAll() {
        Collection<Film> filmList = films.values().stream().toList();
        log.info("Запрошен список всех фильмов. Получено элементов: {}", filmList.size());
        return filmList;
    }

    @Override
    public Film getById(Long id) {
        Film film = films.get(id);
        log.info("Запрошен фильм с id {}.", id);
        if (film == null) throw new NotFoundException("Фильм с id " + id + " не найден");
        return film;
    }

    @Override
    public Film create(Film film) {
        film.setId(getUniqueId());
        films.put(film.getId(), film);
        log.info("Создан фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = getById(film.getId());
        films.put(film.getId(), film);
        log.info("Фильм {} обновлён. Новое значение: {}", oldFilm, film);
        return film;
    }

    @Override
    public Collection<Film> getMostPopular(int count) {
        Collection<Film> mostPopularFilms = films.values().stream()
                .sorted(BY_LIKES_DESC)
                .limit(count)
                .toList();
        log.info("Запрошено {} самых популярных фильмов. Получено элементов {}", count, mostPopularFilms.size());
        return mostPopularFilms;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) throw new NotFoundException("Фильм с id " + filmId + " не найден");
        film.getLikes().add(userId);
        log.info("Пользователь с Id {} поставил лайк фильму с Id {}", userId, filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) throw new NotFoundException("Фильм с id " + filmId + " не найден");
        film.getLikes().remove(userId);
        log.info("Пользователь с Id {} удалил лайк фильму с Id {}", userId, filmId);
    }

    private Long getUniqueId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
