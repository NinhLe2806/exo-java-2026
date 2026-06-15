package com.linagora.folderchecker.service;

import com.linagora.folderchecker.client.MockApiClient;
import com.linagora.folderchecker.model.DuplicateFolders;
import com.linagora.folderchecker.model.FolderKey;
import com.linagora.folderchecker.model.GlobalFolder;
import com.linagora.folderchecker.model.InconsistenciesResponse;
import com.linagora.folderchecker.model.InconsistencySummary;
import com.linagora.folderchecker.model.MissingFolder;
import com.linagora.folderchecker.model.NameMismatch;
import com.linagora.folderchecker.model.UserFolder;
import com.linagora.folderchecker.model.UserScopedFolder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FolderConsistencyService {

    private static final int USER_FOLDER_FETCH_CONCURRENCY = 8;

    private final MockApiClient mockApiClient;

    public FolderConsistencyService(MockApiClient mockApiClient) {
        this.mockApiClient = mockApiClient;
    }

    public Mono<InconsistenciesResponse> findInconsistencies() {
        Mono<List<String>> users = mockApiClient.fetchUsers().cache();
        Mono<List<UserScopedFolder>> userFolders = users
            .flatMapMany(Flux::fromIterable)
            .flatMap(this::fetchUserScopedFolders, USER_FOLDER_FETCH_CONCURRENCY)
            .collectList();
        Mono<List<GlobalFolder>> globalFolders = mockApiClient.fetchGlobalFolders();

        return Mono.zip(users, userFolders, globalFolders)
            .map(tuple -> compare(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    InconsistenciesResponse compare(
        List<String> users,
        List<UserScopedFolder> userFolders,
        List<GlobalFolder> globalFolders
    ) {
        Set<String> knownUsers = new LinkedHashSet<>(users);
        List<UserScopedFolder> globalScopedFolders = globalFolders.stream()
            .map(UserScopedFolder::from)
            .toList();

        List<DuplicateFolders> duplicateEntries = new ArrayList<>();
        duplicateEntries.addAll(findDuplicates("userFolders", userFolders));
        duplicateEntries.addAll(findDuplicates("globalFolders", globalScopedFolders));

        Map<FolderKey, UserScopedFolder> userFolderByKey = firstByKey(userFolders);
        Map<FolderKey, UserScopedFolder> globalFolderByKey = firstByKey(globalScopedFolders);

        List<MissingFolder> missingFromGlobal = userFolderByKey.entrySet().stream()
            .filter(entry -> !globalFolderByKey.containsKey(entry.getKey()))
            .map(Map.Entry::getValue)
            .map(MissingFolder::from)
            .sorted(MISSING_FOLDER_COMPARATOR)
            .toList();

        List<MissingFolder> missingFromUserFolders = globalFolderByKey.entrySet().stream()
            .filter(entry -> knownUsers.contains(entry.getKey().user()))
            .filter(entry -> !userFolderByKey.containsKey(entry.getKey()))
            .map(Map.Entry::getValue)
            .map(MissingFolder::from)
            .sorted(MISSING_FOLDER_COMPARATOR)
            .toList();

        List<NameMismatch> nameMismatches = userFolderByKey.entrySet().stream()
            .filter(entry -> globalFolderByKey.containsKey(entry.getKey()))
            .map(entry -> toNameMismatch(entry.getKey(), entry.getValue(), globalFolderByKey.get(entry.getKey())))
            .filter(mismatch -> !mismatch.userFolderName().equals(mismatch.globalFolderName()))
            .sorted(Comparator.comparing(NameMismatch::user).thenComparing(NameMismatch::id))
            .toList();

        List<MissingFolder> globalFoldersForUnknownUsers = globalFolderByKey.entrySet().stream()
            .filter(entry -> !knownUsers.contains(entry.getKey().user()))
            .map(Map.Entry::getValue)
            .map(MissingFolder::from)
            .sorted(MISSING_FOLDER_COMPARATOR)
            .toList();

        int total = missingFromGlobal.size()
            + missingFromUserFolders.size()
            + nameMismatches.size()
            + duplicateEntries.size()
            + globalFoldersForUnknownUsers.size();

        InconsistencySummary summary = new InconsistencySummary(
            total,
            missingFromGlobal.size(),
            missingFromUserFolders.size(),
            nameMismatches.size(),
            duplicateEntries.size(),
            globalFoldersForUnknownUsers.size()
        );

        return new InconsistenciesResponse(
            summary,
            missingFromGlobal,
            missingFromUserFolders,
            nameMismatches,
            duplicateEntries,
            globalFoldersForUnknownUsers
        );
    }

    private Flux<UserScopedFolder> fetchUserScopedFolders(String user) {
        return mockApiClient.fetchFoldersForUser(user)
            .flatMapMany(Flux::fromIterable)
            .map(folder -> UserScopedFolder.from(user, folder));
    }

    private static Map<FolderKey, UserScopedFolder> firstByKey(List<UserScopedFolder> folders) {
        return folders.stream()
            .collect(Collectors.toMap(
                folder -> new FolderKey(folder.user(), folder.id()),
                Function.identity(),
                (first, ignored) -> first
            ));
    }

    private static List<DuplicateFolders> findDuplicates(String source, List<UserScopedFolder> folders) {
        return folders.stream()
            .collect(Collectors.groupingBy(folder -> new FolderKey(folder.user(), folder.id())))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> toDuplicate(source, entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(DuplicateFolders::source)
                .thenComparing(DuplicateFolders::user)
                .thenComparing(DuplicateFolders::id))
            .toList();
    }

    private static DuplicateFolders toDuplicate(String source, FolderKey key, List<UserScopedFolder> folders) {
        List<String> names = folders.stream()
            .map(UserScopedFolder::name)
            .distinct()
            .sorted()
            .toList();
        return new DuplicateFolders(source, key.user(), key.id(), names, folders.size());
    }

    private static NameMismatch toNameMismatch(
        FolderKey key,
        UserScopedFolder userFolder,
        UserScopedFolder globalFolder
    ) {
        return new NameMismatch(key.user(), key.id(), userFolder.name(), globalFolder.name());
    }

    private static final Comparator<MissingFolder> MISSING_FOLDER_COMPARATOR = Comparator
        .comparing(MissingFolder::user)
        .thenComparing(MissingFolder::id);
}
