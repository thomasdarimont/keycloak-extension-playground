package com.github.thomasdarimont.keycloak.backupcodes;

public class BackupCode {

    private final String id;

    private final String code;

    private final long createdAt;

    public BackupCode(String id, String code, long createdAt) {
        this.id = id;
        this.code = code;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "BackupCode{" +
                "id='" + id + '\'' +
                ", code=REDACTED" +
                ", createdAt=" + createdAt +
                '}';
    }
}
