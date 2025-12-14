package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.exceptions.InsufficientFundsException;
import io.github.tasoula.front_ui.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
public class CashService {
    @Autowired
    private WebClient webClient;

    public Mono<Void> deposit(String login, BigDecimal amount){
       return webClient .post()
               .uri("http://api-gateway/cash/deposit")
               .contentType(MediaType.APPLICATION_JSON) // Указываем тип контента как JSON
                .bodyValue(amount)
                .retrieve()
               .onStatus(status -> !status.is2xxSuccessful(), this::handlePaymentErrorStatus)
                .bodyToMono(Void.class);
    }



    public Mono<Void> withdraw(String login, BigDecimal amount){
        return webClient .post()
                .uri("http://api-gateway/cash/withdraw")
                .contentType(MediaType.APPLICATION_JSON) // Указываем тип контента как JSON
                .bodyValue(amount)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), this::handlePaymentErrorStatus)
                .bodyToMono(Void.class);
    }

    private Mono<? extends Throwable> handlePaymentErrorStatus(ClientResponse response) {
        HttpStatus status = (HttpStatus) response.statusCode();

        if (HttpStatus.PAYMENT_REQUIRED.equals(status)) {
            return Mono.error(new InsufficientFundsException("Операция не прошла (недостаточно средств)"));
        } else if (HttpStatus.NOT_FOUND.equals(status)) {
            return Mono.error(new NoSuchElementException("Операция не прошла (счет не найден)"));
        } else if (HttpStatus.BAD_REQUEST.equals(status)) {
            return Mono.error(new RuntimeException("Операция не прошла (неверный запрос)"));
        } else if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            return Mono.error(new RuntimeException("Операция не прошла (внутрення ошибка сервера платежей)"));
        } else if (HttpStatus.FORBIDDEN.equals(status)) {
            return Mono.error(new RuntimeException("Доступ запрещен"));
        } else if (HttpStatus.UNAUTHORIZED.equals(status)) {
            return Mono.error(new RuntimeException("Ошибка аутентификации"));
        }
        else {
            String errorMessage = "Операция не прошла (Unexpected status code): " + status;
            return Mono.error(new RuntimeException(errorMessage));
        }
    }

}
