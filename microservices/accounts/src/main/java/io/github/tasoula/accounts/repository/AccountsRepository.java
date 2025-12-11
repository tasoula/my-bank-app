package io.github.tasoula.accounts.repository;

import io.github.tasoula.accounts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountsRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLogin(String login);
}
