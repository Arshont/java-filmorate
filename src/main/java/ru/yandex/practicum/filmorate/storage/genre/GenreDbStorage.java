package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final String SELECT_ALL = "SELECT id, name FROM genres ORDER BY id";
    private static final String SELECT_BY_ID = "SELECT id, name FROM genres WHERE id = ?";
    private static final String SELECT_BY_IDS = "SELECT id, name FROM genres WHERE id IN (%s) ORDER BY id";

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Collection<Genre> getAll() {
        return jdbcTemplate.query(SELECT_ALL, genreRowMapper);
    }

    @Override
    public Optional<Genre> getById(int id) {
        return jdbcTemplate.query(SELECT_BY_ID, genreRowMapper, id).stream().findFirst();
    }

    @Override
    public Collection<Genre> getByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        return jdbcTemplate.query(String.format(SELECT_BY_IDS, placeholders), genreRowMapper, ids.toArray());
    }
}
