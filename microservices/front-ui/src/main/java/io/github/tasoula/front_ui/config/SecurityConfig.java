package io.github.tasoula.front_ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
                                    .pathMatchers("/signup").permitAll()
                                    .anyExchange().authenticated();
                        }
                )
                // Форма логина для пользователей
                /*           .formLogin(form -> form.authenticationSuccessHandler(successHandler())
                         )
               /*            .logout(logoutSpec -> logoutSpec
                                   // По умолчанию URL для разлогинивания - /logout (GET).
                                   // После успешного разлогинивания редирект на /login?logout.
                                   // Если хотите другой редирект, можно указать так:
                                   .logoutSuccessHandler(new RedirectServerLogoutSuccessHandler()) //URI.create("/login?logout")))
                           )

                */
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

    @Bean
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


}
