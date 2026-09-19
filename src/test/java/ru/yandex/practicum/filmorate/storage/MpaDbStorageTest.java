package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void getAll_shouldReturnFiveRatingsOrderedById() {
        List<Mpa> mpaList = List.copyOf(mpaStorage.getAll());

        assertThat(mpaList).hasSize(5);
        assertThat(mpaList).extracting(Mpa::getId).containsExactly(1, 2, 3, 4, 5);
        assertThat(mpaList).extracting(Mpa::getName).containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void getById_shouldReturnRating() {
        Optional<Mpa> mpa = mpaStorage.getById(3);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(value -> {
                    assertThat(value).hasFieldOrPropertyWithValue("id", 3);
                    assertThat(value).hasFieldOrPropertyWithValue("name", "PG-13");
                });
    }

    @Test
    void getById_shouldReturnEmptyForUnknownId() {
        assertThat(mpaStorage.getById(9999)).isEmpty();
    }
}
