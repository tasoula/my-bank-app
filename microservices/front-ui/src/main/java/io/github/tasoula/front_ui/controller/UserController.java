package io.github.tasoula.front_ui.controller;


import io.github.tasoula.front_ui.dto.CashOperationDto;
import io.github.tasoula.front_ui.dto.UserDto;
import io.github.tasoula.front_ui.enums.OperationEnum;
import io.github.tasoula.front_ui.exceptions.InsufficientFundsException;
import io.github.tasoula.front_ui.service.AccountService;
import io.github.tasoula.front_ui.service.CashService;
import io.github.tasoula.front_ui.service.UserService;
import io.github.tasoula.front_ui.validation.groups.UpdateGroup;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final AccountService accountService;
    private final CashService cashService;

    public UserController(UserService userService, AccountService accountService, CashService cashService) {
        this.userService = userService;
        this.accountService = accountService;
        this.cashService = cashService;
    }

    @GetMapping("/main")
    public Mono<String> mainPage(//@RegisteredOAuth2AuthorizedClient("front-ui") OAuth2AuthorizedClient authorizedClient,
                                 @AuthenticationPrincipal OidcUser oidcUser,
                                 Model model,
                                 WebSession session) {

        moveErrorsToModel("userAccountErrors", session, model);
        moveErrorsToModel("cashErrors", session, model);

        String login = oidcUser.getUserInfo().getPreferredUsername();

        return userService.findByUsername(login)
                .flatMap(user -> {// todo Передаем в модель пользователя со списком его счетов
                    model.addAttribute("login", login);
                    model.addAttribute("name", user.getName());
                    model.addAttribute("email", user.getEmail());
                    model.addAttribute("birthdate", user.getBirthdate());
                    model.addAttribute("balance", user.getBalance());
                 //   model.addAttribute("users", userService.getOthers(user.getLogin()));
                    // todo так же в модель надо передать список доступных валют с курсами
                 //   model.addAttribute("availableCurrencies", accountService.getCurrencies());
                 //   model.addAttribute("currentUserAccounts", accountService.getUserAccounts(user.getId()));
                    // model.addAttribute("users", userService.getUsers()) // если нам нужно выбирать пользователя из списка,
                    // но всех пользователей банка выводить неправильно, т.к. их очень много
                    // поэтому правиленее вводить самим, например его номер телефона или другой уникальный идентификатор
                    // т.к. телефона у нас нет, то будем считать, что фамилия и имя уникально или добавить к фамилии и иени email
                    return Mono.just("main");
                });
    }

    private void moveErrorsToModel(String errorAttribute, WebSession session, Model model){
        List<String> errors = session.getAttribute(errorAttribute);
        if (errors != null) {
            model.addAttribute(errorAttribute, errors);
            session.getAttributes().remove(errorAttribute);  // 2. Очищаем сессию сразу после извлечения
        }
    }

     @PostMapping("/user/editUser")
    public Mono<String> editUser(@AuthenticationPrincipal OidcUser oidcUser,
                                @Validated(UpdateGroup.class) @ModelAttribute UserDto dto,
                                BindingResult bindingResult,
                                 WebSession session) {

         if (bindingResult.hasErrors()) {
             List<String> errors = new ArrayList<>();
             bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
             session.getAttributes().put("userAccountErrors", errors);
             return Mono.just("redirect:/main"); // Возвращаем страницу с ошибками
         }

         String login = oidcUser.getUserInfo().getPreferredUsername();
         return userService.updateUser(login, dto)
                 .then(Mono.just("redirect:/main"))
                 .onErrorResume(RuntimeException.class, ex -> {
                     session.getAttributes().put("userAccountErrors", List.of(ex.getMessage()));
                     return Mono.just("redirect:/main"); // Возвращаем страницу с ошибками
                 });
     }

    @PostMapping("/user/cash")
    public Mono<String> cash(@AuthenticationPrincipal OidcUser oidcUser,
                             @Valid CashOperationDto cashDto,
                             BindingResult bindingResult,
                             WebSession session)
    {
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
            session.getAttributes().put("cashErrors", errors);
            return Mono.just("redirect:/main"); // Возвращаем страницу с ошибками
        }
        String login = oidcUser.getUserInfo().getPreferredUsername();

        Mono<Void> operationMono;

        switch (cashDto.getAction()){
            case OperationEnum.DEPOSIT -> operationMono = cashService.deposit(login, cashDto.getAmount());
            case OperationEnum.WITHDRAW -> operationMono = cashService.withdraw(login, cashDto.getAmount());
            default -> operationMono = Mono.error(new IllegalArgumentException("Неизвестная операция"));
        };

        // Обрабатываем результат операции реактивно
        return operationMono
                .then(Mono.just("redirect:/main")) // Если успешно, редиректим на главную
                .onErrorResume(Exception.class, ex -> {//InsufficientFundsException.class, ex -> {
                    // 1. Если недостаточно средств, добавляем ошибку в сессию и редиректим
                    List<String> errors = new ArrayList<>();
                    errors.add(ex.getMessage());
                    session.getAttributes().put("cashErrors", errors);
                    return Mono.just("redirect:/main");
                })
                .onErrorResume(Exception.class, ex -> {
                    // 2. Для остальных ошибок показываем экран с описанием ошибки
                    // Вместо редиректа на "redirect:/main", возвращаем имя шаблона ошибки
                    // или можно добавить ошибку в сессию и редиректить на специальную страницу ошибки.

                    // Пример 1: Редирект на специальную страницу, передавая ошибку через сессию/query param
                    session.getAttributes().put("generalError", ex.getMessage());
                    return Mono.just("redirect:/errorPage");

                    // Пример 2: Возвращаем имя представления (например, "errorTemplate.html")
                    // return Mono.just("errorTemplate");
                });

      //  return Mono.just("redirect:/main");
    }

    /*
    @PostMapping("/user/accounts/open")
    public Mono<String> createAccount(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono){
        accountService.createAccount();
        return Mono.just("redirect:/main");
    }

    @PostMapping("/user/accounts/{accountId}/close")
    public Mono<String> deleteAccount(@PathVariable UUID accountId){
        accountService.deleteAccount(accountId);
        return Mono.just("redirect:/main");
    }


    @PostMapping("/user/cash")
    public Mono<String> cashOperation(
            @ModelAttribute CashOperationDto dto,
            Model model) {
        BigDecimal amount = dto.getAmount();
        if(dto.getAction() == OperationEnum.WITHDRAW){
            amount = amount.multiply(BigDecimal.valueOf(-1));
        }
        accountService.cashTransaction(dto.getAccountId(), amount);
        return Mono.just("redirect:/main");
    }

    @PostMapping("/user/transfer/own")
    public Mono<String> transfer(
            @ModelAttribute TransferDto dto,
            Model model) {
        accountService.transferTransaction(dto.getFrom_account_id(), dto.getTo_account_id(), dto.getAmount());
        return Mono.just("redirect:/main");
    }

    @PostMapping("/user/transfer/other")
    public Mono<String> transferOther(
            @ModelAttribute TransferOtherDto dto,
            Model model) {

        return accountService.transferToOther(dto)
                .then(Mono.just("redirect:/main"));
    }

    /*   @PostMapping("/user/{login}/cash")
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
*/
    // Вспомогательные методы для валидации (заглушки)
 /*   private List<String> validatePassword(String password, String confirmPassword) {
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

       private Mono<String> prepareMainPageWithErrors(Mono<UserDetails> userDetailsMono, Model model, String errorAttribute, List<String> errors) {
        // Здесь должна быть логика подготовки главной страницы с ошибками
        // Это упрощенная реализация - в реальном приложении нужно сохранять состояние
        return mainPage(userDetailsMono, model).doOnSuccess(ignore -> {
            model.addAttribute(errorAttribute, errors);
        });
    }

 /*      @PostMapping("/user/{login}/exchange")
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
  /*  @AllArgsConstructor
    @Getter
    @Setter
    public static class UserInfo {
        private String login;
        private String name;
    }

   */
}
