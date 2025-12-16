package io.github.tasoula.front_ui.dto;

import io.github.tasoula.front_ui.enums.OperationEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashOperationDto {

    @NotNull(message = "Сумма не может быть пустой")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0.01")
    private BigDecimal amount;

    @NotNull(message = "Действие не может быть пустым")
    private OperationEnum action;
}
