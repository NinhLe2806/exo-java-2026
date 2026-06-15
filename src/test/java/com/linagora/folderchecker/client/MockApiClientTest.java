package com.linagora.folderchecker.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.linagora.folderchecker.model.UserFolder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class MockApiClientTest {

    private final List<String> requestedPaths = new CopyOnWriteArrayList<>();

    private HttpServer server;
    private MockApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", this::handleRequest);
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        WebClient webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
        client = new MockApiClient(webClient);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void fetchFoldersForUserKeepsEmailCompatibleWithMockApiRoutes() {
        StepVerifier.create(client.fetchFoldersForUser("john@linagora.com"))
            .assertNext(folders -> assertThat(folders)
                .containsExactly(new UserFolder("folder-1", "Inbox")))
            .verifyComplete();

        assertThat(requestedPaths).containsExactly("/users/john@linagora.com/folders");
    }

    private void handleRequest(HttpExchange exchange) {
        requestedPaths.add(exchange.getRequestURI().getRawPath());
        byte[] response = "[{\"id\":\"folder-1\",\"name\":\"Inbox\"}]".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        try {
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
