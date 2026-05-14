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


    // Мб, немного корявая валидации в плане конфликта сообщений: одно и то же сообщение на пустой логин и логин с пробелами.
    // Так сделано (@NotBlank -> @NotNull), чтобы избежать конфликта в тесте с тем, что порядок обработки правил валидации не фиксирован
    // Буду рад совету, как сделать лучше
    // Это я про то, будто @NotBlank использовать правильнее.
    // Но поскольку регулярка покрывает и пустую строку/строку без пробелов, получалосбь пересечение ошибок валидации. То есть вылетало сразу две.
    // Сейчас за @NotBlank отвечает чисто регулярка. Насколько это правильно именно в рамках этих зон отвественности?
    @NotNull(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Пробелы и другие пробельные символы в логине не допускаются")
    private String login;

    // Логика подстановки login при пустом name вынесена в UserService.
    // Я спрашивал в контексте её размещения. То есть, должна ли она находиться в UserService?
    // Или может корректнее поместить её в UserController? Логически будто бы правильнее в UserService,
    // поскольку контроллер, по идее, не должен отвечать ни за что кроме проброса данных в сервисы. Так?
    private String name;

    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}

