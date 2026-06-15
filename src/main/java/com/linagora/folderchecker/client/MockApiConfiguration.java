package com.linagora.folderchecker.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(MockApiProperties.class)
public class MockApiConfiguration {

    @Bean
    WebClient mockApiWebClient(MockApiProperties properties, WebClient.Builder builder) {
        return builder
            .baseUrl(properties.baseUrl())
            .build();
    }
}
