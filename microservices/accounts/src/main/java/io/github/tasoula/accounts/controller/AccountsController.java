package io.github.tasoula.accounts.controller;

import io.github.tasoula.accounts.exceptions.UserNotFoundException;
import io.github.tasoula.accounts.model.User;
import io.github.tasoula.accounts.service.AccountsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
public class AccountsController {

    private final AccountsService service;

    public AccountsController(AccountsService service) {
        this.service = service;
    }

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


    @GetMapping("/api")
    public String mainPage(Authentication authentication) {
        return "Accounts servie answer";
    }

    @PostMapping("create-if-not-exists")
    public User createIfNotExists(Authentication authentication){
        Jwt principal = (Jwt)authentication.getPrincipal();
        String login = principal.getClaimAsString("preferred_username");

        Optional<User> userOpt = service.find(login);
        if(userOpt.isEmpty()){
            String name = principal.getClaimAsString("name");
            String email = principal.getClaimAsString("email");


            String birthdateString = principal.getClaimAsString("birthdate");
            LocalDate birthdate = (birthdateString != null) ? LocalDate.parse(birthdateString, DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null;

            return service.create(login, name, email, birthdate);
        }

        return userOpt.get();
    }


    @GetMapping("/user/{login}")
    public User findUser(@PathVariable String login, HttpServletResponse response) throws IOException {
        Optional<User> userOpt = service.find(login);
        if(userOpt.isEmpty()){
            throw new UserNotFoundException(login);
        }
        return userOpt.get();
    }

    @PostMapping("user/update/{login}")
    public User update(@PathVariable String login, @RequestBody User updUser){
        return service.update(login, updUser);
    }
}
