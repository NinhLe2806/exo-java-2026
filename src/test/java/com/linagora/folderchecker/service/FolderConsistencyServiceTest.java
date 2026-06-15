package com.linagora.folderchecker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.linagora.folderchecker.client.MockApiClient;
import com.linagora.folderchecker.model.GlobalFolder;
import com.linagora.folderchecker.model.InconsistenciesResponse;
import com.linagora.folderchecker.model.UserScopedFolder;
import java.util.List;
import org.junit.jupiter.api.Test;

class FolderConsistencyServiceTest {

    private final FolderConsistencyService service = new FolderConsistencyService(mock(MockApiClient.class));

    @Test
    void compareReportsEverySupportedInconsistencyType() {
        List<String> users = List.of("alice@example.com", "bob@example.com");
        List<UserScopedFolder> userFolders = List.of(
            new UserScopedFolder("alice@example.com", "1", "Inbox"),
            new UserScopedFolder("alice@example.com", "2", "Receipts"),
            new UserScopedFolder("alice@example.com", "2", "Receipts copy"),
            new UserScopedFolder("bob@example.com", "3", "Projects")
        );
        List<GlobalFolder> globalFolders = List.of(
            new GlobalFolder("1", "alice@example.com", "Inbox renamed"),
            new GlobalFolder("4", "alice@example.com", "Archive"),
            new GlobalFolder("4", "alice@example.com", "Archive"),
            new GlobalFolder("5", "charlie@example.com", "Unknown user folder")
        );

        InconsistenciesResponse response = service.compare(users, userFolders, globalFolders);

        assertThat(response.summary().nameMismatches()).isEqualTo(1);
        assertThat(response.summary().missingFromGlobal()).isEqualTo(2);
        assertThat(response.summary().missingFromUserFolders()).isEqualTo(1);
        assertThat(response.summary().duplicateEntries()).isEqualTo(2);
        assertThat(response.summary().globalFoldersForUnknownUsers()).isEqualTo(1);
        assertThat(response.summary().total()).isEqualTo(7);

        assertThat(response.nameMismatches())
            .extracting("user", "id", "userFolderName", "globalFolderName")
            .containsExactly(org.assertj.core.groups.Tuple.tuple(
                "alice@example.com",
                "1",
                "Inbox",
                "Inbox renamed"
            ));
        assertThat(response.missingFromGlobal())
            .extracting("user", "id", "name")
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("alice@example.com", "2", "Receipts"),
                org.assertj.core.groups.Tuple.tuple("bob@example.com", "3", "Projects")
            );
        assertThat(response.missingFromUserFolders())
            .extracting("user", "id", "name")
            .containsExactly(org.assertj.core.groups.Tuple.tuple("alice@example.com", "4", "Archive"));
        assertThat(response.globalFoldersForUnknownUsers())
            .extracting("user", "id", "name")
            .containsExactly(org.assertj.core.groups.Tuple.tuple(
                "charlie@example.com",
                "5",
                "Unknown user folder"
            ));
    }
}
