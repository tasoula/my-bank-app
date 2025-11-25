package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.model.Account;
import io.github.tasoula.front_ui.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
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

    public List<Account> getUserAccounts(UUID id) {
        // логика хромает
        // todo запрос в сервис аккаунтов, там лезем в репозиторий и возвращаем все аккаунты пользователя
        List<Account> accList = new ArrayList<>();
        accList.addAll(repository.values());
        return accList;
    }
}
