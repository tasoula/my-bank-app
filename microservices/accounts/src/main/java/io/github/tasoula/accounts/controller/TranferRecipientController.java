package io.github.tasoula.accounts.controller;

import io.github.tasoula.accounts.model.TransferRecipient;
import io.github.tasoula.accounts.service.TransferRecipientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TranferRecipientController {
    private final TransferRecipientService service;

    public TranferRecipientController(TransferRecipientService service) {
        this.service = service;
    }

    @GetMapping("/transfer_recipients/{login}")
    public List<TransferRecipient> getRecipients(@PathVariable String login){
        List<TransferRecipient> users = service.others(login);
        return users;
    }
}
