package io.github.tasoula.front_ui.service;

import io.github.tasoula.front_ui.dto.UserDto;
import io.github.tasoula.front_ui.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService implements ReactiveUserDetailsService {

    @Autowired
    private WebClient webClient;

    private final Map<String, User> repository; // заменить на Map<UUID, User>
    User other;

    public UserService() {
    //    this.passwordEncoder = passwordEncoder;

        repository = new HashMap<>();
        other = new User(UUID.randomUUID(),
                "otherUser",
                "qwertyui",
                "Иванов Иван Иванович",
                "ivanov_other@mail.ru",
                LocalDate.parse("2002-11-12"));
        repository.put("otherUser", other);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.containsKey(username) ? Mono.just(repository.get(username)) : Mono.empty();
    }

    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public Mono<String> callAccountsService(OAuth2AuthorizedClient authorizedClient, OidcUser oidcUser) {
    /*    return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("front-ui")
                        .principal("ivanov")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue)
                .flatMap(accessToken -> webClient.get()
                        .uri("http://localhost:8070/accounts/api")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(String.class)
                );
*/


        return webClient.get()
                .uri("http://localhost:8070/accounts/api")
                .retrieve()
                .bodyToMono(String.class);
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

    public Mono<?> updateUser(User user, UserDto updDto) {
        User updated = new User(
                user.getId(),
                (updDto.getLogin() == null || updDto.getLogin().isEmpty()) ? user.getLogin() : updDto.getLogin(),
                (updDto.getPassword() == null || updDto.getPassword().isEmpty()) ? user.getPassword() : updDto.getPassword(),
                (updDto.getName() == null || updDto.getName().isEmpty()) ? user.getName() : updDto.getName(),
                (updDto.getEmail() == null || updDto.getEmail().isEmpty()) ? user.getEmail() : updDto.getEmail(),
                (updDto.getBirthdate() == null) ? user.getBirthdate() : updDto.getBirthdate()
        );

        repository.remove(user.getLogin());
        repository.put(updated.getLogin(), updated);
        return Mono.just(updated);
    }

    public List<User> getOthers(String login) {
        return List.of(other);
    }
}


