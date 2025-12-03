package io.github.tasoula.front_ui.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        // Используем реактивную версию фильтра
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        oauth2Filter.setDefaultOAuth2AuthorizedClient(true);
        oauth2Filter.setDefaultClientRegistrationId("front-ui");

        return WebClient.builder()
                .filter(oauth2Filter)
                .build();
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