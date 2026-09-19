package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.utils.TestDataFactory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void create_shouldSaveFilmWithMpaAndGenres() {
        Film created = filmStorage.create(TestDataFactory.film("Матрица", 3, 1, 2));

        assertThat(created.getId()).isNotNull().isPositive();
        assertThat(created.getName()).isEqualTo("Матрица");
        assertThat(created.getDescription()).isEqualTo("Описание фильма Матрица");
        assertThat(created.getReleaseDate()).isEqualTo(LocalDate.of(2000, 5, 20));
        assertThat(created.getDuration()).isEqualTo(120);
        assertThat(created.getMpa()).isNotNull();
        assertThat(created.getMpa().getId()).isEqualTo(3);
        assertThat(created.getMpa().getName()).isEqualTo("PG-13");
        assertThat(created.getGenres()).extracting(Genre::getName).containsExactly("Комедия", "Драма");
    }

    @Test
    void create_shouldReturnGenresSortedByIdWithoutDuplicates() {
        Film created = filmStorage.create(TestDataFactory.film("Дубли", 1, 5, 1, 5, 3));

        assertThat(created.getGenres()).extracting(Genre::getId).containsExactly(1, 3, 5);
    }

    @Test
    void create_shouldSaveFilmWithoutGenres() {
        Film created = filmStorage.create(TestDataFactory.film("Без жанров", 2));

        assertThat(created.getGenres()).isEmpty();
        assertThat(created.getMpa().getId()).isEqualTo(2);
    }

    @Test
    void getById_shouldReturnSavedFilm() {
        Film created = filmStorage.create(TestDataFactory.film("Интерстеллар", 4, 2));

        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Интерстеллар");
                    assertThat(film.getGenres()).extracting(Genre::getId).containsExactly(2);
                });
    }

    @Test
    void getById_shouldReturnEmptyForUnknownId() {
        assertThat(filmStorage.getById(9999L)).isEmpty();
    }

    @Test
    void getAll_shouldReturnAllFilmsWithTheirGenres() {
        filmStorage.create(TestDataFactory.film("Первый", 1, 1));
        filmStorage.create(TestDataFactory.film("Второй", 2, 2, 3));

        Collection<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName).containsExactly("Первый", "Второй");
        assertThat(List.copyOf(films).get(1).getGenres()).extracting(Genre::getId).containsExactly(2, 3);
    }

    @Test
    void update_shouldReplaceFieldsAndGenres() {
        Film created = filmStorage.create(TestDataFactory.film("Старое имя", 1, 1, 2));

        Film changed = TestDataFactory.film("Новое имя", 5, 4);
        changed.setId(created.getId());
        changed.setDuration(200);
        Film updated = filmStorage.update(changed);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getDuration()).isEqualTo(200);
        assertThat(updated.getMpa().getName()).isEqualTo("NC-17");
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(4);
    }

    @Test
    void addLike_shouldBeCountedOnceForTheSameUser() {
        Film film = filmStorage.create(TestDataFactory.film("Лайки", 1));
        User user = userStorage.create(TestDataFactory.user("liker"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.getMostPopular(10)).hasSize(1);
    }

    @Test
    void deleteLike_shouldMoveFilmDownInPopularList() {
        Film popular = filmStorage.create(TestDataFactory.film("Популярный", 1));
        Film other = filmStorage.create(TestDataFactory.film("Другой", 1));
        User user1 = userStorage.create(TestDataFactory.user("user1"));
        User user2 = userStorage.create(TestDataFactory.user("user2"));

        filmStorage.addLike(popular.getId(), user1.getId());
        filmStorage.addLike(popular.getId(), user2.getId());
        filmStorage.addLike(other.getId(), user1.getId());

        assertThat(filmStorage.getMostPopular(10))
                .extracting(Film::getId)
                .containsExactly(popular.getId(), other.getId());

        filmStorage.deleteLike(popular.getId(), user1.getId());
        filmStorage.deleteLike(popular.getId(), user2.getId());

        assertThat(filmStorage.getMostPopular(10))
                .extracting(Film::getId)
                .containsExactly(other.getId(), popular.getId());
    }

    @Test
    void getMostPopular_shouldSortByLikesDescAndRespectCount() {
        Film film1 = filmStorage.create(TestDataFactory.film("Один лайк", 1));
        Film film2 = filmStorage.create(TestDataFactory.film("Три лайка", 1));
        Film film3 = filmStorage.create(TestDataFactory.film("Два лайка", 1));
        User user1 = userStorage.create(TestDataFactory.user("u1"));
        User user2 = userStorage.create(TestDataFactory.user("u2"));
        User user3 = userStorage.create(TestDataFactory.user("u3"));

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user3.getId());
        filmStorage.addLike(film3.getId(), user1.getId());
        filmStorage.addLike(film3.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user1.getId());

        assertThat(filmStorage.getMostPopular(10))
                .extracting(Film::getId)
                .containsExactly(film2.getId(), film3.getId(), film1.getId());

        assertThat(filmStorage.getMostPopular(2))
                .extracting(Film::getId)
                .containsExactly(film2.getId(), film3.getId());
    }

    @Test
    void getMostPopular_shouldIncludeFilmsWithoutLikes() {
        Film film = filmStorage.create(TestDataFactory.film("Без лайков", 1));

        assertThat(filmStorage.getMostPopular(10))
                .extracting(Film::getId)
                .containsExactly(film.getId());
    }
}
