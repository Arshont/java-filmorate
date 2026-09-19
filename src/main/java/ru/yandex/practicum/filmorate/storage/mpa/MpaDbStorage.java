package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private static final String SELECT_ALL = "SELECT id, name FROM mpa ORDER BY id";
    private static final String SELECT_BY_ID = "SELECT id, name FROM mpa WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Collection<Mpa> getAll() {
        return jdbcTemplate.query(SELECT_ALL, mpaRowMapper);
    }

    @Override
    public Optional<Mpa> getById(int id) {
        return jdbcTemplate.query(SELECT_BY_ID, mpaRowMapper, id).stream().findFirst();
    }
}
