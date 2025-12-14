package io.github.tasoula.cash.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Конфигурация авторизации для сервлетного стека
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // Разрешить доступ без аутентификации к /actuator/health
                                .requestMatchers("/actuator/health").permitAll()
                                // Требовать аутентификацию для всех остальных запросов
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer -> // Включаем поддержку Resource Server
                        oauth2ResourceServer.jwt(jwt -> {})
                );
        // Использование стандартной формы входа (или других механизмов по умолчанию)
        //  .httpBasic(Customizer.withDefaults());

        // Если нужно отключить CSRF (раскомментируйте, если требуется, хотя обычно это не рекомендуется)
        // .csrf(csrf -> csrf.disable());

        return http.build();
    }
}





