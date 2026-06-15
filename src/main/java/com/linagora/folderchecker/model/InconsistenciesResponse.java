package com.linagora.folderchecker.model;

import java.util.List;

public record InconsistenciesResponse(
    InconsistencySummary summary,
    List<MissingFolder> missingFromGlobal,
    List<MissingFolder> missingFromUserFolders,
    List<NameMismatch> nameMismatches,
    List<DuplicateFolders> duplicateEntries,
    List<MissingFolder> globalFoldersForUnknownUsers
) {
}
