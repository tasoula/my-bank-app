package io.github.tasoula.accounts.service;

import io.github.tasoula.accounts.exceptions.UserNotFoundException;
import io.github.tasoula.accounts.model.User;
import io.github.tasoula.accounts.repository.AccountsRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class AccountsService {
    private final AccountsRepository repository;

    public AccountsService(AccountsRepository repository) {
        this.repository = repository;
    }

    public Optional<User>  find(String login) {
        return repository.findByLogin(login);
    }

    public User create(String login, String name, String email, LocalDate birthdate) {
        User newUser = new User();
        newUser.setLogin(login);
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setBirthdate(birthdate);
        newUser.setBalance(BigDecimal.ZERO);
        return repository.save(newUser);
    }

    public User update(String login, User updUser) {
        // todo эти изменеия надо бы прокидывать на Keycloak. Но где это лушче делать: здесь или в Front-UI
        // регистрацию нового пользователя тожен надо прокидывать, но она будет скорее всего на UI, хотя, может и тут
        User existingUser = repository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException(login));

        if(!StringUtils.isEmpty(updUser.getName()))
            existingUser.setName(updUser.getName());

        if(!StringUtils.isEmpty(updUser.getEmail()))
            existingUser.setEmail(updUser.getEmail());

        if(updUser.getBirthdate() != null) existingUser.setBirthdate(updUser.getBirthdate());
        if(updUser.getBalance() != null) existingUser.setBalance(updUser.getBalance());
        return repository.save(existingUser);
    }
}
