package io.github.tasoula.cash.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.function.Consumer;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced // Делает RestClient "discovery-aware"
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient restClient(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder loadBalancedBuilder) {

        // Используем специальный интерцептор для OAuth2 в стеке сервлетов
        OAuth2ClientHttpRequestInterceptor oauth2Interceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager); // Указываем registrationId по умолчанию

        // Устанавливаем registrationId по умолчанию через атрибут запроса
        // Используем статический метод-хелпер из RequestAttributeClientRegistrationIdResolver
        Consumer<Map<String, Object>> clientRegistrationId =
                RequestAttributeClientRegistrationIdResolver.clientRegistrationId("cash");


        return loadBalancedBuilder
                .requestInterceptor(oauth2Interceptor)
                .defaultRequest(requestSpec -> requestSpec.attributes(clientRegistrationId))
                .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials() // Включаем получение токена с помощью client_credentials
                .refreshToken() // Также включаем использование refresh_token
                .build());

        return manager;
    }

}

/*
// Затем просто используется в сервисах:

@Autowired
private WebClient webClient;

public Mono<String> callAnotherService() {
    return webClient.get()
            .uri("http://other-service/api")
            .retrieve()
            .bodyToMono(String.class);
}

 */