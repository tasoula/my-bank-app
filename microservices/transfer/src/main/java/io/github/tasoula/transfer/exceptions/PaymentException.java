package io.github.tasoula.transfer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PAYMENT_REQUIRED, reason = "Недостаточно средств")
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
