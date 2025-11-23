package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.dto.UserRegistrationDto;
import io.github.tasoula.front_ui.exceptions.UserAlreadyExistsException;
import jakarta.validation.constraints.*;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final Map<String, UserRegistrationDto> repository = new HashMap<>();

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.containsKey(username) ? Mono.just(repository.get(username)) : Mono.empty();
    }

    public Mono<UserDetails> createUser(UserRegistrationDto userRegistrationDto) {
        // todo обращение в сервис Accounts
        // будем создавать по умолчанию рублевый счет
        return Mono.fromCallable(() -> {
            if (repository.containsKey(userRegistrationDto.getLogin())) {
                throw new UserAlreadyExistsException("пользователь с таким логином уже зарегистрирован");
            } else {
                String password = passwordEncoder.encode(userRegistrationDto.getPassword());
                UserRegistrationDto savedUser = new UserRegistrationDto(
                        // todo в классе для UserDetails будет id пользователя
                        userRegistrationDto.getLogin(),
                        password,
                        password, //todo повтор пароля можно не хранить, нужен другой dto для одного зашифрованного пароля
                        userRegistrationDto.getName(), // фамилия и имя пользователя
                        userRegistrationDto.getEmail(),
                        userRegistrationDto.getBirthdate()
                );

                repository.put(userRegistrationDto.getLogin(), savedUser);
                return savedUser;
            }
        });
    }

    public Mono<Void> deleteUser(UserRegistrationDto user) {
        repository.remove(user.getLogin());
        return Mono.empty();
    }
}


