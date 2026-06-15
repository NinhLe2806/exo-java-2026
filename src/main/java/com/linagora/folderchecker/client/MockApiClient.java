package com.linagora.folderchecker.client;

import com.linagora.folderchecker.model.GlobalFolder;
import com.linagora.folderchecker.model.UserFolder;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MockApiClient {

    private static final ParameterizedTypeReference<List<String>> USERS_TYPE = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<UserFolder>> USER_FOLDERS_TYPE = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<GlobalFolder>> GLOBAL_FOLDERS_TYPE = new ParameterizedTypeReference<>() {
    };

    private final WebClient webClient;

    public MockApiClient(WebClient mockApiWebClient) {
        this.webClient = mockApiWebClient;
    }

    public Mono<List<String>> fetchUsers() {
        return webClient.get()
            .uri("/users")
            .retrieve()
            .bodyToMono(USERS_TYPE);
    }

    public Mono<List<UserFolder>> fetchFoldersForUser(String email) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/users/")
                .path(email)
                .path("/folders")
                .build())
            .retrieve()
            .bodyToMono(USER_FOLDERS_TYPE);
    }

    public Mono<List<GlobalFolder>> fetchGlobalFolders() {
        return webClient.get()
            .uri("/folders")
            .retrieve()
            .bodyToMono(GLOBAL_FOLDERS_TYPE);
    }
}
