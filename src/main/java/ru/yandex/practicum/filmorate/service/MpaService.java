package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<Mpa> getAll() {
        Collection<Mpa> mpaList = mpaStorage.getAll();
        log.info("Запрошен список всех рейтингов MPA. Получено элементов: {}", mpaList.size());
        return mpaList;
    }

    public Mpa getById(int id) {
        Mpa mpa = mpaStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
        log.info("Запрошен и получен рейтинг MPA с id {}.", id);
        return mpa;
    }
}
