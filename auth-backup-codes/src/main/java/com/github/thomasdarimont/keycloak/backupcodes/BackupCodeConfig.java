package com.github.thomasdarimont.keycloak.backupcodes;

import lombok.Data;
import org.keycloak.credential.hash.Pbkdf2PasswordHashProviderFactory;
import org.keycloak.credential.hash.Pbkdf2Sha256PasswordHashProviderFactory;
import org.keycloak.models.RealmModel;

@Data
public class BackupCodeConfig {

    private int backupCodeCount = Integer.getInteger("keycloak_backup_code_count", 10);
    private int backupCodeLength = Integer.getInteger("keycloak_backup_code_length", 8);

    private String hashingProviderId = System.getProperty("keycloak_backup_code_hash_provider", Pbkdf2Sha256PasswordHashProviderFactory.ID);
    private int backupCodeHashIterations = Integer.getInteger("keycloak_backup_code_hash_iterations", 1000);

    private static final BackupCodeConfig DEFAULT = new BackupCodeConfig();

    public static BackupCodeConfig getConfig(RealmModel realmModel) {
        // TODO make backup-code handling configurable via realm
        return DEFAULT;
    }

}
