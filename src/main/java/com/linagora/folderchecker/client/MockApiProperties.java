package com.linagora.folderchecker.client;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "mock-api")
public record MockApiProperties(@NotBlank String baseUrl) {
}
