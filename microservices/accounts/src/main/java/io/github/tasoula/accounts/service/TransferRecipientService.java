package io.github.tasoula.accounts.service;

import io.github.tasoula.accounts.model.TransferRecipient;
import io.github.tasoula.accounts.model.User;
import io.github.tasoula.accounts.repository.TransferRecipientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferRecipientService {
    private final TransferRecipientRepository repository;

    public TransferRecipientService(TransferRecipientRepository repository) {
        this.repository = repository;
    }

    public List<TransferRecipient> others(String login){
        return repository.findByLoginNot(login);
    }
}
