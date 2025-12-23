package io.github.tasoula.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


//todo как-то не очень, что у нас 2 похожих dto.
// может сразу из Front UI передавать dto c логином отправителя? Хотя его неплохо бы доставать из токена
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferOperationDto {
    String loginFrom;
    String loginTo;
    BigDecimal amount;
}



