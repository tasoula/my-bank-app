package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.exceptions.ResourceNotFoundException;
import io.github.tasoula.front_ui.model.Account;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AccountService {

    private List<String> CARRENCIES = List.of("RUB", "EUR", "USD");
    private final Map<UUID, Account> repository = new HashMap<>();


    public List<String> getCurrencies() {
        return CARRENCIES;
    }

    public Mono<Account> createAccount(/*User user*/){
        // todo создавать аккаунт для конкретного пользователя
        Account newAccount = new Account(UUID.randomUUID(), CARRENCIES.get(0), BigDecimal.TEN);
        repository.put(newAccount.getId(), newAccount);
        return Mono.just(newAccount);
    }


    public Mono<Account> deleteAccount(UUID accountId){
        //удалить аккаунт по id
        return Mono.just(repository.remove(accountId));
    }

    //todo возможно, возвращаемый тип должен быть Mono
    public List<Account> getUserAccounts(UUID id) {
        // логика хромает
        // todo запрос в сервис аккаунтов, там лезем в репозиторий и возвращаем все аккаунты пользователя
        List<Account> accList = new ArrayList<>();
        accList.addAll(repository.values());
        return accList;
    }

    public Mono<Void> cashTransaction(UUID accountId, BigDecimal amount) {
        Account account = repository.get(accountId);
        if(account != null){
            account.setBalance(account.getBalance().add(amount));
        }
        return Mono.empty();
    }

    public Mono<Void> transferTransaction(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        Account fromAccount = repository.get(fromAccountId);
        if(fromAccount == null) {
            throw new ResourceNotFoundException("Счет " + fromAccountId + " не найден");
        }
        Account toAccount = repository.get(toAccountId);
        if(toAccount == null) {
            throw new ResourceNotFoundException("Счет " + toAccountId + " не найден");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        //todo по заданю у пользователя может быть только один счет в каждой валюте.
        // Получается, что при переводе между своими счетами надо конвертировать валюту?
        return Mono.empty();
    }
}
