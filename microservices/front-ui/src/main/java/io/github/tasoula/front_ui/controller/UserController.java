package io.github.tasoula.front_ui.controller;

import io.github.tasoula.front_ui.dto.UserRegistrationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public Mono<String> mainPage(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono, Model model) {

        return userDetailsMono.cast(UserRegistrationDto.class)
                .flatMap(user->{
                    // todo Передаем в модель пользователя со списком его счетов
                    model.addAttribute("login", user.getLogin());
                    model.addAttribute("name", user.getName());
                    model.addAttribute("birthdate", user.getBirthdate());
                    model.addAttribute("users", List.of(
                            // Здесь должна быть логика получения данных других пользователей
                            // Отображать только тех, у кого есть счета в заданной валюте?
                            // Или отображать всех, но если счета в нужной валюте нет, то выдать ошибку?
                            // Наверное 2е, т.к. пользователю в этом случае будет понятнее, что делать
                            new UserInfo("user1", "Петров Петр"),
                            new UserInfo("user2", "Сидорова Анна")));
                    // todo так же в модель надо передать список доступных валют с курсами
                    return Mono.just("main");
                });
    }

    /*
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

       @PostMapping("/user/{login}/exchange")
   public String performExchange(@PathVariable String login,
                                 @RequestParam(value = "amountSell", required = false) Double amountSell,
                                 @RequestParam(value = "amountBuy", required = false) Double amountBuy,
                                 @RequestParam("action") String action, // REQUIRED PARAMETER!
                                 Model model) {

       // Validation: Only ONE of amountSell or amountBuy should be present.
       if ((amountSell != null && amountBuy != null) || (amountSell == null && amountBuy == null)) {
           model.addAttribute("exchangeErrors", List.of("Заполните только одно поле: 'Купить' или 'Продать'."));
           // Redisplay the form with the error message.  You'll need to reload currency rates here too.
           return "your-view-name"; // Replace with the actual view name.
       }

       try {
           if ("sell".equals(action)) {
               // Perform the sell operation using amountSell
               System.out.println("Selling " + amountSell + " " + ...); // Implement your logic
           } else if ("buy".equals(action)) {
               // Perform the buy operation using amountBuy
               System.out.println("Buying " + amountBuy + " " + ...);   // Implement your logic
           } else {
               // Handle invalid action (shouldn't happen if the buttons are configured correctly)
               model.addAttribute("exchangeErrors", List.of("Неизвестное действие."));
               return "your-view-name";
           }

           // Success - redirect or redisplay the form with a success message
           return "redirect:/user/" + login; // Example: Redirect to the user's page.

       } catch (Exception e) {
           // Handle exceptions (e.g., insufficient funds, invalid amount)
           model.addAttribute("exchangeErrors", List.of("Ошибка обмена: " + e.getMessage()));
           // Redisplay the form with the error message
           return "your-view-name";
       }
   }
*/
    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserInfo {
        private String login;
        private String name;
    }
}
