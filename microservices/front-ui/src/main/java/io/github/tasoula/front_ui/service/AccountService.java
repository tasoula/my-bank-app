package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.dto.TransferDto;
import io.github.tasoula.front_ui.dto.TransferOtherDto;
import io.github.tasoula.front_ui.exceptions.PaymentException;
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

    private Account otherUserAccount = new Account(UUID.randomUUID(), "RUB", BigDecimal.valueOf(1000));

    public AccountService() {
       repository.put(otherUserAccount.getId(), otherUserAccount);
    }


    public List<String> getCurrencies() {
        // где хранится список валют
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
    public List<Account> getUserAccounts(UUID userId) {
        // todo запрос в сервис аккаунтов, там лезем в репозиторий и возвращаем все аккаунты пользователя
        List<Account> accList = new ArrayList<>();
        accList.addAll(repository.values());
        return accList;
    }

    //получение счета пользователя в заданной валюте
    public Mono<Account> getUserAccounts(UUID userId, String currency) {
        // логика хромает
        // todo запрос в сервис аккаунтов, там лезем в репозиторий и возвращаем все аккаунты пользователя

        return Mono.just(otherUserAccount);
    }

    public Mono<Void> cashTransaction(UUID accountId, BigDecimal amount) {
        Account account = repository.get(accountId);
        if(account != null){
            account.setBalance(account.getBalance().add(amount));
        }
        return Mono.empty();
    }

    public Mono<Void> transferTransaction(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {

        Account fromAccount = repository.get( fromAccountId);
        if(fromAccount == null) {
            throw new ResourceNotFoundException("Счет " + fromAccountId + " не найден");
        }
        if(fromAccount.getBalance().compareTo(amount) < 0){
            throw new PaymentException("недостаточно средств");
        }
        Account toAccount = repository.get(toAccountId);
        if(toAccount == null) {
            throw new ResourceNotFoundException("Счет " + toAccountId + " не найден");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        //todo по заданю у пользователя может быть только один счет в каждой валюте.
        // Получается, что при переводе между своими счетами надо конвертировать валюту
        return Mono.empty();
    }

    public Mono<Void> transferToOther(TransferOtherDto dto) {
        transferTransaction(dto.getFrom_account_id(), otherUserAccount.getId(), dto.getAmount());

        return Mono.empty();
    }
}
