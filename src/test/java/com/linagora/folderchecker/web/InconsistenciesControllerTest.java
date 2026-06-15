package com.linagora.folderchecker.web;

import static org.mockito.Mockito.when;

import com.linagora.folderchecker.model.InconsistenciesResponse;
import com.linagora.folderchecker.model.InconsistencySummary;
import com.linagora.folderchecker.service.FolderConsistencyService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(InconsistenciesController.class)
class InconsistenciesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FolderConsistencyService folderConsistencyService;

    @Test
    void getInconsistenciesReturnsServiceResponse() {
        InconsistenciesResponse response = new InconsistenciesResponse(
            new InconsistencySummary(0, 0, 0, 0, 0, 0),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
        when(folderConsistencyService.findInconsistencies()).thenReturn(Mono.just(response));

        webTestClient.get()
            .uri("/inconsistencies")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.summary.total").isEqualTo(0)
            .jsonPath("$.missingFromGlobal").isArray()
            .jsonPath("$.missingFromUserFolders").isArray()
            .jsonPath("$.nameMismatches").isArray();
    }
}
