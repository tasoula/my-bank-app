package io.github.tasoula.accounts.service;

import io.github.tasoula.accounts.dto.CashOperationDto;
import io.github.tasoula.accounts.dto.TransferOperationDto;
import io.github.tasoula.accounts.exceptions.PaymentException;
import io.github.tasoula.accounts.exceptions.UserNotFoundException;
import io.github.tasoula.accounts.model.User;
import io.github.tasoula.accounts.repository.AccountsRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void deposit(CashOperationDto dto) {
        User user = repository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new UserNotFoundException(dto.getLogin()));
        user.setBalance(user.getBalance().add(dto.getAmount()));
        repository.save(user);
    }

    public void withdraw(CashOperationDto dto) {
        User user = repository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new UserNotFoundException(dto.getLogin()));
        BigDecimal currentBalance = user.getBalance();
        if(currentBalance.compareTo(dto.getAmount()) < 0){
            throw new PaymentException("Недостаточно средств");
        }
        user.setBalance(currentBalance.subtract(dto.getAmount()));
        repository.save(user);
    }

    @Transactional
    public void transfer(TransferOperationDto dto) {
        User sender = repository.findByLogin(dto.getLoginFrom())
                .orElseThrow(() -> new UserNotFoundException(dto.getLoginFrom()));

        BigDecimal amount = dto.getAmount();
        if(sender.getBalance().compareTo(amount) < 0){
            throw new PaymentException("Недостаточно средств");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        repository.save(sender);

        User recipient = repository.findByLogin(dto.getLoginTo())
                .orElseThrow(() -> new UserNotFoundException(dto.getLoginTo()));
        recipient.setBalance(recipient.getBalance().add(amount));
        repository.save(recipient);
    }
}
