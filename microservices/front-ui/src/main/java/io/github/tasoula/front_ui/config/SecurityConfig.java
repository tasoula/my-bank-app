package io.github.tasoula.front_ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;


import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
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
                                   // .pathMatchers("/signup", "/login").permitAll()
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
             //   .logout(logout -> logout
              //          .logoutSuccessHandler(oidcLogoutSuccessHandler())
             //   )
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

    @Bean
    public ServerAuthenticationSuccessHandler successHandler() {
        return (webFilterExchange, authentication) -> {
            ServerWebExchange exchange = webFilterExchange.getExchange();
            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            exchange.getResponse().getHeaders().setLocation(URI.create("/main"));
            return exchange.getResponse().setComplete();
        };
    }

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

    // Обработчик выхода из системы (Logout handler)
    // В реальном приложении нужно также отправлять запрос на завершение сессии в Keycloak (RP-initiated logout)
 /*   private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        // Это пример, требующий доработки для взаимодействия с OIDC провайдером
        return new HttpStatusCollectingServerLogoutSuccessHandler(HttpStatus.SEE_OTHER);
    }

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
