package io.github.tasoula.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferOperationDto {
    String loginFrom;
    String loginTo;
    BigDecimal amount;
}



