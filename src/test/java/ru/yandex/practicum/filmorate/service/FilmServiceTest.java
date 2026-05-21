package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService();
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A thief who steals corporate secrets through dream-sharing technology.");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    @Test
    void createFilm_shouldAssignUniqueId() {
        Film film = createValidFilm();

        Film createdFilm = filmService.create(film);

        assertNotNull(createdFilm.getId());
        assertTrue(createdFilm.getId() > 0);
    }

    @Test
    void createFilm_shouldStoreFilm() {
        Film film = createValidFilm();

        Film createdFilm = filmService.create(film);

        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDescription(), createdFilm.getDescription());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getDuration(), createdFilm.getDuration());
    }

    @Test
    void createMultipleFilms_shouldAssignDifferentIds() {
        Film film1 = createValidFilm();
        film1.setName("Film 1");

        Film film2 = createValidFilm();
        film2.setName("Film 2");

        Film createdFilm1 = filmService.create(film1);
        Film createdFilm2 = filmService.create(film2);

        assertNotEquals(createdFilm1.getId(), createdFilm2.getId());
    }

    @Test
    void updateFilm_shouldUpdateExistingFilm() {
        Film film = createValidFilm();
        Film createdFilm = filmService.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Inception");
        updateFilm.setDescription("Updated description.");
        updateFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        updateFilm.setDuration(150);

        Film updatedFilm = filmService.update(updateFilm);

        assertEquals(createdFilm.getId(), updatedFilm.getId());
        assertEquals("Updated Inception", updatedFilm.getName());
        assertEquals("Updated description.", updatedFilm.getDescription());
        assertEquals(150, updatedFilm.getDuration());
    }

    @Test
    void updateFilm_shouldThrowNotFoundIfFilmNotExists() {
        Film film = createValidFilm();
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> filmService.update(film));
    }

    @Test
    void getAll_shouldReturnAllFilms() {
        Film film1 = createValidFilm();
        film1.setName("Film 1");

        Film film2 = createValidFilm();
        film2.setName("Film 2");

        filmService.create(film1);
        filmService.create(film2);

        Collection<Film> films = filmService.getAll();

        assertEquals(2, films.size());
        assertTrue(films.stream().anyMatch(f -> f.getName().equals("Film 1")));
        assertTrue(films.stream().anyMatch(f -> f.getName().equals("Film 2")));
    }

    @Test
    void getAll_shouldReturnEmptyCollectionWhenNoFilms() {
        Collection<Film> films = filmService.getAll();

        assertTrue(films.isEmpty());
    }
}