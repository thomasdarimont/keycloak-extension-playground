package com.github.thomasdarimont.keycloak.backupcodes;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.UserCredentialModel;

@RequiredArgsConstructor
public class BackupCode {

    public static final String CREDENTIAL_TYPE = "backup-code";

    private final String id;

    private final String code;

    public static UserCredentialModel toUserCredentialModel(String input) {

        UserCredentialModel backupCode = new UserCredentialModel(null, CREDENTIAL_TYPE, input, false);

        return backupCode;
    }

    @Override
    public String toString() {
        return "BackupCode{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    public String getCode() {
        return code;
    }
}
