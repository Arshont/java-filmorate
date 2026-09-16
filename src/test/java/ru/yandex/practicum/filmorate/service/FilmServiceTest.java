package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(new InMemoryFilmStorage(), userStorage);
        userService = new UserService(userStorage);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A thief who steals corporate secrets through dream-sharing technology.");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    private Film createAndSaveFilm(String name) {
        Film film = createValidFilm();
        film.setName(name);
        return filmService.create(film);
    }

    private User createAndSaveUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.create(user);
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

    @Test
    void addLike_shouldAddUserLikeToFilm() {
        Film film = createAndSaveFilm("Film 1");
        User user = createAndSaveUser("user1");

        filmService.addLike(film.getId(), user.getId());

        assertEquals(1, filmService.getById(film.getId()).getLikes().size());
        assertTrue(filmService.getById(film.getId()).getLikes().contains(user.getId()));
    }

    @Test
    void addLike_shouldNotDuplicateLikeFromSameUser() {
        Film film = createAndSaveFilm("Film 1");
        User user = createAndSaveUser("user1");

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user.getId());

        assertEquals(1, filmService.getById(film.getId()).getLikes().size());
    }

    @Test
    void addLike_shouldCountLikesOfDifferentUsers() {
        Film film = createAndSaveFilm("Film 1");
        User user1 = createAndSaveUser("user1");
        User user2 = createAndSaveUser("user2");

        filmService.addLike(film.getId(), user1.getId());
        filmService.addLike(film.getId(), user2.getId());

        assertEquals(2, filmService.getById(film.getId()).getLikes().size());
    }

    @Test
    void addLike_shouldThrowNotFoundIfFilmNotExists() {
        User user = createAndSaveUser("user1");

        assertThrows(NotFoundException.class, () -> filmService.addLike(999L, user.getId()));
    }

    @Test
    void addLike_shouldThrowNotFoundIfUserNotExists() {
        Film film = createAndSaveFilm("Film 1");

        assertThrows(NotFoundException.class, () -> filmService.addLike(film.getId(), 999L));
    }

    @Test
    void deleteLike_shouldRemoveUserLikeFromFilm() {
        Film film = createAndSaveFilm("Film 1");
        User user = createAndSaveUser("user1");
        filmService.addLike(film.getId(), user.getId());

        filmService.deleteLike(film.getId(), user.getId());

        assertTrue(filmService.getById(film.getId()).getLikes().isEmpty());
    }

    @Test
    void deleteLike_shouldRemoveOnlyLikeOfGivenUser() {
        Film film = createAndSaveFilm("Film 1");
        User user1 = createAndSaveUser("user1");
        User user2 = createAndSaveUser("user2");
        filmService.addLike(film.getId(), user1.getId());
        filmService.addLike(film.getId(), user2.getId());

        filmService.deleteLike(film.getId(), user1.getId());

        assertEquals(1, filmService.getById(film.getId()).getLikes().size());
        assertTrue(filmService.getById(film.getId()).getLikes().contains(user2.getId()));
    }

    @Test
    void deleteLike_shouldDoNothingIfUserDidNotLikeFilm() {
        Film film = createAndSaveFilm("Film 1");
        User user = createAndSaveUser("user1");

        assertDoesNotThrow(() -> filmService.deleteLike(film.getId(), user.getId()));
        assertTrue(filmService.getById(film.getId()).getLikes().isEmpty());
    }

    @Test
    void deleteLike_shouldThrowNotFoundIfFilmNotExists() {
        User user = createAndSaveUser("user1");

        assertThrows(NotFoundException.class, () -> filmService.deleteLike(999L, user.getId()));
    }

    @Test
    void deleteLike_shouldThrowNotFoundIfUserNotExists() {
        Film film = createAndSaveFilm("Film 1");

        assertThrows(NotFoundException.class, () -> filmService.deleteLike(film.getId(), 999L));
    }

    @Test
    void getMostPopular_shouldSortFilmsByLikesCountDesc() {
        Film unpopularFilm = createAndSaveFilm("Unpopular");
        Film popularFilm = createAndSaveFilm("Popular");
        User user1 = createAndSaveUser("user1");
        User user2 = createAndSaveUser("user2");

        filmService.addLike(unpopularFilm.getId(), user1.getId());
        filmService.addLike(popularFilm.getId(), user1.getId());
        filmService.addLike(popularFilm.getId(), user2.getId());

        List<Film> mostPopular = List.copyOf(filmService.getMostPopular(10));

        assertEquals(2, mostPopular.size());
        assertEquals(popularFilm.getId(), mostPopular.get(0).getId());
        assertEquals(unpopularFilm.getId(), mostPopular.get(1).getId());
    }

    @Test
    void getMostPopular_shouldLimitResultByCount() {
        createAndSaveFilm("Film 1");
        createAndSaveFilm("Film 2");
        createAndSaveFilm("Film 3");

        Collection<Film> mostPopular = filmService.getMostPopular(2);

        assertEquals(2, mostPopular.size());
    }

    @Test
    void getMostPopular_shouldReturnAllFilmsIfCountExceedsFilmsNumber() {
        createAndSaveFilm("Film 1");
        createAndSaveFilm("Film 2");

        Collection<Film> mostPopular = filmService.getMostPopular(10);

        assertEquals(2, mostPopular.size());
    }

    @Test
    void getMostPopular_shouldReturnEmptyCollectionWhenNoFilms() {
        Collection<Film> mostPopular = filmService.getMostPopular(10);

        assertTrue(mostPopular.isEmpty());
    }
}