package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String SELECT_FILMS =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON m.id = f.mpa_id ";

    private static final String SELECT_ALL = SELECT_FILMS + "ORDER BY f.id";

    private static final String SELECT_BY_ID = SELECT_FILMS + "WHERE f.id = ?";

    private static final String SELECT_MOST_POPULAR = SELECT_FILMS +
            "LEFT JOIN film_likes fl ON fl.film_id = f.id " +
            "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, m.id, m.name " +
            "ORDER BY COUNT(fl.user_id) DESC, f.id " +
            "LIMIT ?";

    private static final String SELECT_GENRES_BY_FILM_IDS =
            "SELECT fg.film_id, g.id, g.name " +
                    "FROM film_genres fg " +
                    "JOIN genres g ON g.id = fg.genre_id " +
                    "WHERE fg.film_id IN (%s) " +
                    "ORDER BY fg.film_id, g.id";

    private static final String INSERT_FILM =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String INSERT_FILM_GENRE =
            "MERGE INTO film_genres KEY (film_id, genre_id) VALUES (?, ?)";

    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String MERGE_LIKE = "MERGE INTO film_likes KEY (film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Collection<Film> getAll() {
        return loadGenres(jdbcTemplate.query(SELECT_ALL, filmRowMapper));
    }

    @Override
    public Optional<Film> getById(Long id) {
        return loadGenres(jdbcTemplate.query(SELECT_BY_ID, filmRowMapper, id)).stream().findFirst();
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, film.getMpa().getId());
            }
            return ps;
        }, keyHolder);

        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        saveGenres(filmId, film.getGenres());

        // Перечитываем фильм, чтобы вернуть названия рейтинга и жанров, отсортированные по id
        return getById(filmId).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId());

        jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());
        saveGenres(film.getId(), film.getGenres());

        return getById(film.getId()).orElseThrow();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(MERGE_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbcTemplate.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getMostPopular(int count) {
        return loadGenres(jdbcTemplate.query(SELECT_MOST_POPULAR, filmRowMapper, count));
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = genres.stream()
                .map(Genre::getId)
                .distinct()
                .map(genreId -> new Object[]{filmId, genreId})
                .toList();

        jdbcTemplate.batchUpdate(INSERT_FILM_GENRE, batchArgs);
    }

    private List<Film> loadGenres(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }

        Map<Long, Film> filmsById = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        String placeholders = filmsById.keySet().stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        RowCallbackHandler handler = rs -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            filmsById.get(rs.getLong("film_id")).getGenres().add(genre);
        };

        jdbcTemplate.query(String.format(SELECT_GENRES_BY_FILM_IDS, placeholders),
                handler, filmsById.keySet().toArray());

        return films;
    }
}
