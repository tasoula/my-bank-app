package io.github.tasoula.front_ui.exceptions;

public class NonZeroAccountsException extends RuntimeException{
    public NonZeroAccountsException(String message) {
        super(message);
    }
}
