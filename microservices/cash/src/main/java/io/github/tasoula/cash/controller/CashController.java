package io.github.tasoula.cash.controller;

import io.github.tasoula.cash.dto.CashOperationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@RestController
public class CashController {
    @Autowired
    private RestClient restClient;

   @PostMapping("/deposit")
   public void deposit(Authentication authentication, @RequestBody BigDecimal amount){
       Jwt principal = (Jwt)authentication.getPrincipal();
       String login = principal.getClaimAsString("preferred_username");

        restClient.post()
                .uri("http://accounts-service/deposit")// todo заменить константные строки на данные из application.yml
                .contentType(MediaType.APPLICATION_JSON) // Указываем тип контента как JSON
                .attributes(clientRegistrationId("cash"))
                .body(new CashOperationDto(login, amount))
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> System.out.println("------200 OK"))
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> System.out.println("------" + response.getStatusText()))
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> System.out.println("------" + response.getStatusText()))
              //  .onStatus(status -> !status.is2xxSuccessful(), this::handlePaymentErrorStatus)
               // .bodyToMono(Void.class);
                    .toBodilessEntity();
    }

    @PostMapping("/withdraw")
    public void withdraw(Authentication authentication, @RequestBody BigDecimal amount) {
        Jwt principal = (Jwt) authentication.getPrincipal();
        String login = principal.getClaimAsString("preferred_username");
        ResponseEntity<Void> resp = restClient.post()
                    .uri("http://accounts-service/withdraw")
                    .contentType(MediaType.APPLICATION_JSON) // Указываем тип контента как JSON
                    .attributes(clientRegistrationId("cash"))
                    .body(new CashOperationDto(login, amount))
                    .retrieve()
                    .toBodilessEntity();
    }
}
