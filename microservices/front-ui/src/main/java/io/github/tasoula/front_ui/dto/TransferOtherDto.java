package io.github.tasoula.front_ui.dto;

import io.github.tasoula.front_ui.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferOtherDto {
    String to_login;
    BigDecimal amount;
}
