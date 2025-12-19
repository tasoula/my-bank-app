package io.github.tasoula.transfer.controller;

import io.github.tasoula.transfer.dto.TransferOperationDto;
import io.github.tasoula.transfer.dto.TransferOtherDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@RestController
public class TransferController {
    @Autowired
    private RestClient restClient;

    @PostMapping("/transfer")
    public void transfer(Authentication authentication, @RequestBody TransferOtherDto transferDto) {
        Jwt principal = (Jwt) authentication.getPrincipal();
        String login = principal.getClaimAsString("preferred_username");

        restClient.post()
                .uri("http://accounts-service/transfer")// todo заменить константные строки на данные из application.yml
                .contentType(MediaType.APPLICATION_JSON) // Указываем тип контента как JSON
                .attributes(clientRegistrationId("transfer"))// todo в настройках webClient тоже указавали клиента. Откуда-то надо убрать
                .body(new TransferOperationDto(login, transferDto.getTo_login(), transferDto.getAmount()))
                .retrieve()
                .toBodilessEntity();
    }
}
