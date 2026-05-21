package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id"}) // Как будто бы стоит учитывать и email,
// но тогда может быть нарушена работа HashMap, если email может быть изменён
public class User {
    private Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email некорректен")
    private String email;

    @NotNull(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Пробелы и другие пробельные символы в логине не допускаются")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть пуста")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}

