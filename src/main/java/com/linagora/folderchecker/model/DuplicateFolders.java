package com.linagora.folderchecker.model;

import java.util.List;

public record DuplicateFolders(String source, String user, String id, List<String> names, int occurrences) {
}
