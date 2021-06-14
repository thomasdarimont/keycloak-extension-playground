package com.github.thomasdarimont.keycloak.backupcodes;

import org.keycloak.credential.hash.Pbkdf2Sha256PasswordHashProviderFactory;
import org.keycloak.models.RealmModel;

public class BackupCodeConfig {

    // TODO how to store backup code configuration? can we use password policies for this, or realm attributes?

    private int backupCodeCount = Integer.getInteger("keycloak_backup_code_count", 10);
    private int backupCodeLength = Integer.getInteger("keycloak_backup_code_length", 8);

    private String hashingProviderId = System.getProperty("keycloak_backup_code_hash_provider", Pbkdf2Sha256PasswordHashProviderFactory.ID);
    private int backupCodeHashIterations = Integer.getInteger("keycloak_backup_code_hash_iterations", 1000);

    private static final BackupCodeConfig DEFAULT = new BackupCodeConfig();

    public static BackupCodeConfig getConfig(RealmModel realmModel) {
        return DEFAULT;
    }

    public int getBackupCodeCount() {
        return backupCodeCount;
    }

    public void setBackupCodeCount(int backupCodeCount) {
        this.backupCodeCount = backupCodeCount;
    }

    public int getBackupCodeLength() {
        return backupCodeLength;
    }

    public void setBackupCodeLength(int backupCodeLength) {
        this.backupCodeLength = backupCodeLength;
    }

    public String getHashingProviderId() {
        return hashingProviderId;
    }

    public void setHashingProviderId(String hashingProviderId) {
        this.hashingProviderId = hashingProviderId;
    }

    public int getBackupCodeHashIterations() {
        return backupCodeHashIterations;
    }

    public void setBackupCodeHashIterations(int backupCodeHashIterations) {
        this.backupCodeHashIterations = backupCodeHashIterations;
    }

    @Override
    public String toString() {
        return "BackupCodeConfig{" +
                "backupCodeCount=" + backupCodeCount +
                ", backupCodeLength=" + backupCodeLength +
                ", hashingProviderId='" + hashingProviderId + '\'' +
                ", backupCodeHashIterations=" + backupCodeHashIterations +
                '}';
    }
}
