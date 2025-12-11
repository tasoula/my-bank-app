package io.github.tasoula.accounts.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_accounts")
public class User  {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    String login;
    String name;
    String email;
    LocalDate birthdate;
    BigDecimal balance;
}
