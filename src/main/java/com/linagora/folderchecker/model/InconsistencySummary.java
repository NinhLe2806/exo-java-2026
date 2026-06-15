package com.linagora.folderchecker.model;

public record InconsistencySummary(
    int total,
    int missingFromGlobal,
    int missingFromUserFolders,
    int nameMismatches,
    int duplicateEntries,
    int globalFoldersForUnknownUsers
) {
}
