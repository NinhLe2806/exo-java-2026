package com.linagora.folderchecker.web;

import com.linagora.folderchecker.model.InconsistenciesResponse;
import com.linagora.folderchecker.service.FolderConsistencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class InconsistenciesController {

    private final FolderConsistencyService folderConsistencyService;

    public InconsistenciesController(FolderConsistencyService folderConsistencyService) {
        this.folderConsistencyService = folderConsistencyService;
    }

    @GetMapping("/inconsistencies")
    public Mono<InconsistenciesResponse> getInconsistencies() {
        return folderConsistencyService.findInconsistencies();
    }
}
