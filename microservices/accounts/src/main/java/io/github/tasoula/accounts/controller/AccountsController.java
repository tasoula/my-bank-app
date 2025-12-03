package io.github.tasoula.accounts.controller;

import io.github.tasoula.accounts.model.User;
import org.hibernate.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

@RestController
public class AccountsController {
   /* @Override
    public ResponseEntity<User> userUserIdGet(
            @Parameter(name = "userId", description = "UUID пользователя", required = true, in = ParameterIn.PATH) @PathVariable("userId") UUID userId,
            @Parameter(hidden = true) final ServerWebExchange exchange
    )  {
        return service.getBalance(userId)
                .map(Amount::new)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Amount>build()); // 500
                });
    }

    */


    @GetMapping("/accounts/api")
    public String mainPage(Authentication authentication) {
        return "Accounts servie answer";
    }

}
