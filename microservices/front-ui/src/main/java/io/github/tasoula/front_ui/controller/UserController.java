package io.github.tasoula.front_ui.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Controller
public class UserController {

    @GetMapping("/")
    public Mono<String> redirectToMain() {
        return Mono.just("redirect:/main");
    }

    @GetMapping("/main")
    public Mono<String> mainPage(Model model) {
        // Здесь должна быть логика получения данных пользователя и других пользователей
        // Заглушки для примера:
        model.addAttribute("login", "currentUser");
        model.addAttribute("name", "Иванов Иван");
        model.addAttribute("birthdate", LocalDate.of(1990, 1, 1));
        model.addAttribute("users", List.of(
                new UserInfo("user1", "Петров Петр"),
                new UserInfo("user2", "Сидорова Анна")
        ));
        // Ошибки изначально null
        model.addAttribute("passwordErrors", null);
        model.addAttribute("userAccountErrors", null);
        model.addAttribute("cashErrors", null);
        model.addAttribute("transferOtherErrors", null);

        return Mono.just("main");
    }

    @PostMapping("/user/{login}/editPassword")
    public Mono<String> editPassword(
            @PathVariable String login,
            @RequestParam String password,
            @RequestParam String confirm_password,
            Model model) {

        // Здесь должна быть логика валидации и смены пароля
        List<String> errors = validatePassword(password, confirm_password);

        if (errors.isEmpty()) {
            // Успешная смена пароля
            return Mono.just("redirect:/main");
        } else {
            // Возвращаем на главную с ошибками
            return prepareMainPageWithErrors(model, "passwordErrors", errors);
        }
    }

    @PostMapping("/user/{login}/editUserAccount")
    public Mono<String> editUserAccount(
            @PathVariable String login,
            @RequestParam String name,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
            Model model) {

        // Здесь должна быть логика валидации и обновления данных
        List<String> errors = validateUserAccount(name, birthdate);

        if (errors.isEmpty()) {
            // Успешное обновление данных
            return Mono.just("redirect:/main");
        } else {
            return prepareMainPageWithErrors(model, "userAccountErrors", errors);
        }
    }

    @PostMapping("/user/{login}/cash")
    public Mono<String> cashOperation(
            @PathVariable String login,
            @RequestParam Double value,
            @RequestParam String action,
            Model model) {

        // Здесь должна быть логика операций с деньгами
        List<String> errors = validateCashOperation(value, action);

        if (errors.isEmpty()) {
            // Успешная операция
            return Mono.just("redirect:/main");
        } else {
            return prepareMainPageWithErrors(model, "cashErrors", errors);
        }
    }

    @PostMapping("/user/{login}/transfer")
    public Mono<String> transferToOther(
            @PathVariable String login,
            @RequestParam Double value,
            @RequestParam String to_login,
            Model model) {

        // Здесь должна быть логика перевода денег
        List<String> errors = validateTransfer(value, to_login);

        if (errors.isEmpty()) {
            // Успешный перевод
            return Mono.just("redirect:/main");
        } else {
            return prepareMainPageWithErrors(model, "transferOtherErrors", errors);
        }
    }



    // Вспомогательные методы для валидации (заглушки)
    private List<String> validatePassword(String password, String confirmPassword) {
        // Реализуйте логику валидации пароля
        return List.of();
    }

    private List<String> validateUserAccount(String name, LocalDate birthdate) {
        // Реализуйте логику валидации данных пользователя
        return List.of();
    }

    private List<String> validateCashOperation(Double value, String action) {
        // Реализуйте логику валидации операций с деньгами
        return List.of();
    }

    private List<String> validateTransfer(Double value, String toLogin) {
        // Реализуйте логику валидации перевода
        return List.of();
    }

    private Mono<String> prepareMainPageWithErrors(Model model, String errorAttribute, List<String> errors) {
        // Здесь должна быть логика подготовки главной страницы с ошибками
        // Это упрощенная реализация - в реальном приложении нужно сохранять состояние
        return mainPage(model).doOnSuccess(ignore -> {
            model.addAttribute(errorAttribute, errors);
        });
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserInfo {
        private String login;
        private String name;
    }
}
