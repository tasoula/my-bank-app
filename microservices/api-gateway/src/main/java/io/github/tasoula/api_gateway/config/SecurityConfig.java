package io.github.tasoula.api_gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;



@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Отключение CSRF-защиты
                //.csrf(ServerHttpSecurity.CsrfSpec::disable)
                //.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .authorizeExchange(
                        exchanges -> {
                            exchanges
                                    .pathMatchers("/actuator/health").permitAll()
                                    .anyExchange().authenticated();
                        }
                )
                .oauth2ResourceServer(oauth2ResourceServer -> // Включаем поддержку Resource Server
                        oauth2ResourceServer.jwt(jwt -> {})
                )
                .build();
    }
}





