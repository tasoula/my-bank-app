package io.github.tasoula.front_ui.dto;

import io.github.tasoula.front_ui.validation.Adult;
import io.github.tasoula.front_ui.validation.groups.RegistrationGroup;
import io.github.tasoula.front_ui.validation.groups.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
//@PasswordMatches(groups = {RegistrationGroup.class, PasswordChangeGroup.class})
public class UserDto {
    @NotBlank(message = "Логин не может быть пустым", groups = RegistrationGroup.class)
    @Size(min = 3, max = 20, message = "Логин должен быть от 3 до 20 символов", groups = {RegistrationGroup.class, UpdateGroup.class})
    String login;

    // @NotBlank(message = "Пароль не может быть пустым", groups = {RegistrationGroup.class, PasswordChangeGroup.class})
    // @Size(min = 8, message = "Пароль должен содержать минимум 8 символов",
    //         groups = {RegistrationGroup.class, PasswordChangeGroup.class, UpdateGroup.class})
    // String password;

    // String confirm_password;

    @NotBlank(message = "Фамилия и имя не могут быть пустыми", groups = RegistrationGroup.class)
    @Size(min = 2, message = "Фамилия и имя должны содержать минимум 2 символа",
            groups = {RegistrationGroup.class, UpdateGroup.class})
    String name;

    @NotBlank(message = "Email не может быть пустым", groups = RegistrationGroup.class)
    @Email(message = "Некорректный формат email", groups = {RegistrationGroup.class, UpdateGroup.class})
    String email;

    @NotNull(message = "Дата рождения не может быть пустой", groups = RegistrationGroup.class)
    @Adult(message = "Вам должно быть не менее 18 лет", groups = {RegistrationGroup.class, UpdateGroup.class})
    LocalDate birthdate;

    // для правильной валидации
    public void setName(String name) {
        // Если пришла пустая строка или строка из пробелов, сохраняем null
        this.name = (name == null || name.trim().isEmpty()) ? null : name;
    }
}
