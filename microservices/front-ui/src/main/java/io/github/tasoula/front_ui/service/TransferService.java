package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.dto.TransferOtherDto;
import io.github.tasoula.front_ui.dto.TransferRecipient;
import io.github.tasoula.front_ui.exceptions.PaymentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
public class TransferService {

    @Autowired
    private WebClient webClient;

    public Flux<TransferRecipient> getOthers(String login) {
        return webClient.get()
                .uri("http://api-gateway/accounts/transfer_recipients/" + login)
                .retrieve()
                .bodyToFlux(TransferRecipient.class);
    }


    public Mono<Void> transfer(TransferOtherDto transferDto) {
        return webClient .post()
                .uri("http://api-gateway/transfer/transfer")
                .contentType(MediaType.APPLICATION_JSON) // Указываем тип контента как JSON
                .bodyValue(transferDto)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), this::handlePaymentErrorStatus)
                .bodyToMono(Void.class);
    }

    //todo вынести обработку ошибок в общий класс
    private Mono<? extends Throwable> handlePaymentErrorStatus(ClientResponse response) {
        HttpStatus status = (HttpStatus) response.statusCode();

        if (HttpStatus.PAYMENT_REQUIRED.equals(status)) {
            return Mono.error(new PaymentException("Операция не прошла (недостаточно средств)"));
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
