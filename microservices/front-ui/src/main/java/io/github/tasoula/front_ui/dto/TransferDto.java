package io.github.tasoula.front_ui.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferDto {
    UUID from_account_id;
    UUID to_account_id;
    BigDecimal amount;
}
