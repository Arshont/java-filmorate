package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void getAll_shouldReturnSixGenresOrderedById() {
        List<Genre> genres = List.copyOf(genreStorage.getAll());

        assertThat(genres).hasSize(6);
        assertThat(genres).extracting(Genre::getId).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(genres).extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void getById_shouldReturnGenre() {
        Optional<Genre> genre = genreStorage.getById(1);

        assertThat(genre)
                .isPresent()
                .hasValueSatisfying(value -> {
                    assertThat(value).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(value).hasFieldOrPropertyWithValue("name", "Комедия");
                });
    }

    @Test
    void getById_shouldReturnEmptyForUnknownId() {
        assertThat(genreStorage.getById(9999)).isEmpty();
    }

    @Test
    void getByIds_shouldReturnOnlyExistingGenres() {
        List<Genre> genres = List.copyOf(genreStorage.getByIds(List.of(2, 500, 4)));

        assertThat(genres).extracting(Genre::getId).containsExactly(2, 4);
    }

    @Test
    void getByIds_shouldReturnEmptyListForEmptyInput() {
        assertThat(genreStorage.getByIds(List.of())).isEmpty();
    }
}
