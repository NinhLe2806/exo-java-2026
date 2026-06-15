package com.linagora.folderchecker.model;

public record UserScopedFolder(String user, String id, String name) {

    public static UserScopedFolder from(String user, UserFolder folder) {
        return new UserScopedFolder(user, folder.id(), folder.name());
    }

    public static UserScopedFolder from(GlobalFolder folder) {
        return new UserScopedFolder(folder.user(), folder.id(), folder.name());
    }
}
