package io.github.tasoula.front_ui.controller;

import io.github.tasoula.front_ui.dto.UserRegistrationDto;
import io.github.tasoula.front_ui.exceptions.NonZeroAccountsException;
import io.github.tasoula.front_ui.exceptions.UserAlreadyExistsException;
import io.github.tasoula.front_ui.model.User;
import io.github.tasoula.front_ui.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginController {

    private final UserService userService;
    private final ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private ServerSecurityContextRepository securityContextRepository;

    public LoginController(UserService userService, ReactiveAuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/signup")
    public Mono<String> showSignupForm(WebSession session) {
        return session.changeSessionId()
                    .thenReturn("signup.html");
    }

    @PostMapping("/signup")
    public Mono<String> signup(@Valid @ModelAttribute UserRegistrationDto userRegistrationDto,
                               BindingResult bindingResult,
                               Model model,
                               ServerWebExchange exchange) {
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
            model.addAttribute("errors", errors);
            return Mono.just("signup"); // Возвращаем страницу с ошибками
        }

        // Логика регистрации и аутентификации
        return userService.createUser(userRegistrationDto)
                .flatMap(userDetails -> {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userRegistrationDto.getLogin(),
                            userRegistrationDto.getPassword()
                    );
                    return authenticationManager.authenticate(authentication) // Аутентифицируем пользователя
                            .flatMap(auth -> {
                                // Сохраняем аутентификацию в SecurityContext
                                SecurityContext securityContext = new SecurityContextImpl(auth);
                                return securityContextRepository.save(exchange, securityContext)
                                        .then(Mono.just("redirect:/main"));
                            });
                })
                .onErrorResume(UserAlreadyExistsException.class, ex -> {
                    model.addAttribute("errors", List.of(ex.getMessage()));
                    return Mono.just("signup"); // Возвращаем страницу с ошибками
                });
    }

    @PostMapping("/delete")
    public Mono<String> delete(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono) {

        // пытаемся удалить пользователя
        // если есть ненулевые счета, то выводим сообщение о том, что для нельзя удалить пользователя, у которого есть ненулевые счета
        // если удалить не удалось, то добавляем в модель сообщение исключения, выброшенного методом userService::deleteUser
        // если удаление прошло успешно, то аннулируем сессию и переходим на /login?deleted

        return userDetailsMono
                .cast(User.class)
                .flatMap(userDto -> userService.deleteUser(userDto)
                        .then(Mono.just("redirect:/logout?deleted"))
                        .onErrorResume(NonZeroAccountsException.class, ex -> {
                            System.err.println("User deletion failed due to non-zero accounts: " + ex.getMessage());
                            return Mono.just("Для нельзя удалить пользователя, у которого есть ненулевые счета");
                        })
                        // Обработка любых других исключений, выброшенных userService::deleteUser
                        .onErrorResume(Exception.class, ex -> {
                            System.err.println("User deletion failed with unexpected error: " + ex.getMessage());
                            return Mono.just(ex.getMessage()); // Возвращаем сообщение исключения
                        })
                )
                // Если userDetailsMono пуст (например, пользователь не авторизован или сессия истекла)
                .switchIfEmpty(Mono.just("Пользователь не найден или не авторизован для удаления."));
    }

    @GetMapping("/login")
    public Mono<String> login(WebSession session,
             ServerWebExchange exchange,
                              Model model) {

        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        // Проверяем наличие ключа "error" в карте параметров
        if (queryParams.containsKey("error")) {
            // Логика для запросов типа http://localhost:8080/login?error
            // или http://localhost:8080/login?error=someValue
            model.addAttribute("error", "error");
           }
        if (queryParams.containsKey("logout")) {
            model.addAttribute("logout", "logout");
        }
        if (queryParams.containsKey("deleted")) {
            model.addAttribute("deleted", "deleted");
        }

        return session.changeSessionId()
                .thenReturn("login.html");
    }

    @GetMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(WebSession session, ServerWebExchange exchange) {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        String uri = (queryParams.containsKey("deleted")) ? "/login?deleted" : "/login?logout";

        return session.invalidate()
                .thenReturn(
                        ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(uri))
                                .build()
                );
    }

    private Mono<Boolean> validateUserRegistration(UserRegistrationDto userRegistrationDto, BindingResult bindingResult) {
        // Проверка паролей
     //   if (!userRegistrationDto.getPassword().equals(userRegistrationDto.getConfirm_password())) {
     //       bindingResult.rejectValue("confirm_password", "error.userRegistrationDto", "Пароли не совпадают");
     //   }

        // Проверка возраста
        if (userRegistrationDto.getBirthdate() != null) {
            Period period = Period.between(userRegistrationDto.getBirthdate(), LocalDate.now());
            if (period.getYears() < 18) {
                bindingResult.rejectValue("birthdate", "error.userRegistrationDto", "Пользователь должен быть старше 18 лет");
            }
        }

        return Mono.just(!bindingResult.hasErrors()); // Возвращаем результат валидации
    }
}

