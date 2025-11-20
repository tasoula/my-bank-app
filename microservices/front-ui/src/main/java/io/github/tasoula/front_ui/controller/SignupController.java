package io.github.tasoula.front_ui.controller;

import io.github.tasoula.front_ui.dto.UserRegistrationDto;
import io.github.tasoula.front_ui.exceptions.UserAlreadyExistsException;
import io.github.tasoula.front_ui.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SignupController {

    private final UserService userService;
    private final ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private ServerSecurityContextRepository securityContextRepository;

    public SignupController(UserService userService, ReactiveAuthenticationManager authenticationManager) {
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

        // Логика валидации, которая может быть вынесена в отдельный Mono
        return validateUserRegistration(userRegistrationDto, bindingResult)
                .flatMap(isValid -> {
                    if (!isValid) {
                        List<String> errors = new ArrayList<>();
                        bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
                        model.addAttribute("errors", errors);
                        return Mono.just("signup"); // Возвращаем страницу с ошибками
                    }

                    // Логика регистрации и аутентификации
                    return userService.createUser(userRegistrationDto)
                            .flatMap(userDetails -> {
                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                        userRegistrationDto.getUsername(),
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
                });
    }

    // Отдельный метод для валидации, возвращающий Mono<Boolean>
    private Mono<Boolean> validateUserRegistration(UserRegistrationDto userRegistrationDto, BindingResult bindingResult) {
        // Проверка паролей
        if (!userRegistrationDto.getPassword().equals(userRegistrationDto.getConfirm_password())) {
            bindingResult.rejectValue("confirm_password", "error.userRegistrationDto", "Пароли не совпадают");
        }

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

