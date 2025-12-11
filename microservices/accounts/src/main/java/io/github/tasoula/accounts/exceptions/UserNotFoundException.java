package io.github.tasoula.accounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException  extends RuntimeException{
    public UserNotFoundException(String login) {
        super("Account not found with login: " + login);
    }
}
