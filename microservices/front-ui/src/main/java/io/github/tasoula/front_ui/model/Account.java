package io.github.tasoula.front_ui.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    UUID id;

    //todo в таблице будет ссылка на владельца
    String currency;
    BigDecimal balance;
    //todo created_at? - может и не нужен
}
