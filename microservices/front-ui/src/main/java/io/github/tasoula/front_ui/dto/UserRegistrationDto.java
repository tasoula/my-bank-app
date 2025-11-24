package io.github.tasoula.front_ui.dto;

import io.github.tasoula.front_ui.validation.Adult;
import io.github.tasoula.front_ui.validation.PasswordMatches;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
@PasswordMatches
public class UserRegistrationDto extends PasswordChangeDto{
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 20, message = "Логин должен быть от 3 до 20 символов")
    String login;

    @NotBlank(message = "Фамилия и имя не могут быть пустыми")
    @Size(min = 2, message = "Фамилия и имя должны содержать минимум 2 символа")
    String name; // фамилия и имя пользователя

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    String email;

    @NotNull(message = "Дата рождения не может быть пустой")
    @Adult(message = "Вам должно быть не менее 18 лет")
    LocalDate birthdate;
}
