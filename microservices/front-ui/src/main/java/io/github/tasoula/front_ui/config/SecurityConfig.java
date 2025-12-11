package io.github.tasoula.front_ui.config;

import io.github.tasoula.front_ui.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Отключение CSRF-защиты
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .authorizeExchange(
                        exchanges -> {
                            exchanges
                                    .pathMatchers("/css/**", "/js/**").permitAll()
                                    .pathMatchers("/actuator/health").permitAll()
                                    .anyExchange().authenticated();
                        }
                )
                // Активация OAuth 2.0 Login (Authorization Code Flow)
                .oauth2Login(oauth2 -> oauth2
                        // Spring Security предоставит стандартную страницу входа,
                        // которая содержит ссылки на настроенные провайдеры (Keycloak)
                        .authenticationSuccessHandler(successHandler())
                )
                // Настройка ручки логаута (автоматически чистит сессию)
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler())
                )
             //   .anonymous(anonymous -> anonymous
            //            .principal("guestUser")
            //            .authorities("ROLE_GUEST")
            //            .key("uniqueAnonymousKey")
            //    )
                // Настройка обработки ошибок
                .exceptionHandling(handling -> handling
                        .accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
                )
                .build();
    }

    @Autowired
    private WebClient webClient;

    @Bean
    public ServerAuthenticationSuccessHandler successHandler() {

        return  (webFilterExchange, authentication) -> {

            ServerWebExchange exchange = webFilterExchange.getExchange();

            // 1. Выполнение POST-запроса через WebClient
            Mono<User> webClientCall = webClient.post()
                    .uri("http://api-gateway/accounts/create-if-not-exists")
                    .retrieve()
                    .bodyToMono(User.class)
                    .doOnSuccess(user -> System.out.println("------------------------" + user.getLogin() + " " + user.getName()))
                    .doOnError(error -> {
                        // Логируем ошибку, если запрос к API-Gateway не удался
                        System.err.println("Ошибка при вызове accounts/api: " + error.getMessage());
                    })
                    .onErrorResume(Exception.class, e -> Mono.empty()); // Продолжаем выполнение, даже если запрос к API не удался

            // 2. Установка статуса и заголовка перенаправления
            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            exchange.getResponse().getHeaders().setLocation(URI.create("/main"));

            // 3. Объединение выполнения запроса WebClient и завершения ответа сервера.
            // Используем .then() для выполнения завершения ответа после того, как запрос к API завершится (успешно или с ошибкой).
            return webClientCall.then(exchange.getResponse().setComplete());
        };
    }

    // Замените ваш текущий private метод logoutSuccessHandler() на этот бин:
    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(this.clientRegistrationRepository);

        // Указываем, куда вернуться после того, как Keycloak завершит свою сессию
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:8080/");

        return oidcLogoutSuccessHandler;
    }

 /*   @Bean
    public ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(this.clientRegistrationRepository);

        // Указываем URI, на который пользователь будет перенаправлен после
        // завершения сессии в Keycloak и в вашем приложении.
        // Здесь используется значение по умолчанию "{baseUrl}", которое обычно
        // разрешается в корень вашего приложения, например, "http://localhost:8080/".
        logoutSuccessHandler.setPostLogoutRedirectUri("/");//(URI.create("/"));

        return logoutSuccessHandler;
    }

  */

 /*   @Bean
    public ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        // Замените этот URL на актуальный URL вашего Keycloak сервера
        // В продакшене лучше получать issuer-uri из настроек application.yml
        URI logoutUri = URI.create("http://localhost:8082/realms/master/protocol/openid-connect/logout");

     /*    OidcClientInitiatedServerLogoutSuccessHandler handler = new OidcClientInitiatedServerLogoutSuccessHandler(
                // Использует бин ReactiveClientRegistrationRepository, который Spring Boot предоставляет автоматически
                clientRegistrationRepository
        );
        handler.setPostLogoutRedirectUri("/main");
        return handler;

        */

        // Альтернативный, более ручной подход для формирования URI выхода:

        /*     return (exchange, authentication) -> Mono.defer(() -> {
            // Применяем параметры для перенаправления обратно в приложение после выхода из Keycloak
            URI location = UriComponentsBuilder.fromUri(logoutUri)
                    .queryParam("client_id", "front-ui") // Ваш client-id из application.yml
                    .queryParam("post_logout_redirect_uri", "http://localhost:8080/") // Куда вернуться
                    .build()
                    .toUri();

            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            exchange.getResponse().getHeaders().setLocation(location);
            return exchange.getResponse().setComplete();
        });

         */

 //   }

  /*  @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        return (exchange, authentication) -> {
            ServerHttpResponse response = exchange.getExchange().getResponse();
            response.setStatusCode(HttpStatus.FOUND);
            response.getHeaders().setLocation(URI.create("/login?logout"));
            return response.setComplete();
        };
    }
*/


 /*    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

   @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

  */


}
