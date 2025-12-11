package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.dto.UserDto;
import io.github.tasoula.front_ui.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserService  {

    @Autowired
    private WebClient webClient;

    public Mono<User> findByUsername(String login) {
        return webClient.get()
                .uri("http://api-gateway/accounts/user/" + login)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<UserDetails> createUser(UserDto userRegistrationDto) {
        // todo обращение в сервис Accounts
        // будем создавать по умолчанию рублевый счет
     /*   return Mono.fromCallable(() -> {
            if (repository.containsKey(userRegistrationDto.getLogin())) {
                throw new UserAlreadyExistsException("пользователь с таким логином уже зарегистрирован");
            } else {
                String password = passwordEncoder.encode(userRegistrationDto.getPassword());
                User savedUser = new User(
                        UUID.randomUUID(),
                        userRegistrationDto.getLogin(),
                        password,
                        userRegistrationDto.getName(), // фамилия и имя пользователя
                        userRegistrationDto.getEmail(),
                        userRegistrationDto.getBirthdate()
                );

                repository.put(userRegistrationDto.getLogin(), savedUser);
                return savedUser;
            }
        });


      */
        return Mono.empty();
    }

    public Mono<String> deleteUser(User user) {
        return Mono.empty();
    }

    public Mono<User> updateUser(String login, UserDto updDto) {
        User updated = new User(
                null,
                updDto.getLogin(),
                updDto.getName(),
                updDto.getEmail() ,
                updDto.getBirthdate() ,
                null
        );


        return webClient.post()
                .uri("http://api-gateway/accounts/user/update/" + login)
                //.contentType(MediaType.APPLICATION_JSON) // Указываем тип контента как JSON
                .bodyValue(updated)
                .retrieve()
                .bodyToMono(User.class)
                .doOnSuccess(user -> System.out.println("------------------------" + user.getLogin() + " " + user.getName()))
                .doOnError(error -> {
                    // Логируем ошибку, если запрос к API-Gateway не удался
                    System.err.println("Ошибка при вызове accounts/api: " + error.getMessage());
                })
                .onErrorResume(Exception.class, e -> Mono.empty());
    }
}


