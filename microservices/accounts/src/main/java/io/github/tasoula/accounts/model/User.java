package io.github.tasoula.accounts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User  {
    UUID id;
    String login;
    String password;
    String name;
    String email;
    LocalDate birthdate;
}
