package io.github.tasoula.accounts.repository;

import io.github.tasoula.accounts.model.TransferRecipient;
import io.github.tasoula.accounts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferRecipientRepository extends JpaRepository<TransferRecipient, String> {

    List<TransferRecipient> findByLoginNot(String login);
}
