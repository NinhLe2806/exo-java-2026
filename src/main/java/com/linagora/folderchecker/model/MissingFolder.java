package com.linagora.folderchecker.model;

public record MissingFolder(String user, String id, String name) {

    public static MissingFolder from(UserScopedFolder folder) {
        return new MissingFolder(folder.user(), folder.id(), folder.name());
    }
}
