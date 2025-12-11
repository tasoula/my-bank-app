package io.github.tasoula.front_ui.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    UUID id;
    String login;
    String name;
    String email;
    LocalDate birthdate;
    BigDecimal balance;
}