package io.github.tasoula.accounts.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity // Замена @EnableWebFluxSecurity
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
                                .anyRequest().hasRole("Accounts-access")//.authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer -> // Включаем поддержку Resource Server
                        oauth2ResourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
                // Использование стандартной формы входа (или других механизмов по умолчанию)
              //  .httpBasic(Customizer.withDefaults());

        // Если нужно отключить CSRF (раскомментируйте, если требуется, хотя обычно это не рекомендуется)
        // .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess == null || !resourceAccess.containsKey("accounts-service")) {
                return Collections.emptyList();
            }

            Map<String, Object> bankAccounts = (Map<String, Object>) resourceAccess.get("accounts-service");
            List<String> roles = (List<String>) bankAccounts.get("roles");

            return roles.stream()
                    // Spring Security по умолчанию добавляет префикс ROLE_ при проверке hasRole()
                    .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                    .collect(Collectors.toList());
        });

        return jwtAuthenticationConverter;
    }

}





