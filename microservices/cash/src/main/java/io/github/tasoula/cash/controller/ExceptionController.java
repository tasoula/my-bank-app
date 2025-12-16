package io.github.tasoula.cash.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { RestClientResponseException.class })
    protected ResponseEntity<Object> handleRestClientResponse(RestClientResponseException ex, WebRequest request) {
        // Извлекаем оригинальный HTTP статус из исключения
        HttpStatus originalStatus = HttpStatus.valueOf(ex.getRawStatusCode());

        // Можем также передать тело ответа или заголовки, если нужно.
        // ex.getResponseBodyAsString()

        // Возвращаем ответ с оригинальным статусом и телом ошибки,
        // чтобы front-ui мог его прочитать
        return handleExceptionInternal(ex, ex.getResponseBodyAsString(),
                new HttpHeaders(), originalStatus, request);
    }
}
