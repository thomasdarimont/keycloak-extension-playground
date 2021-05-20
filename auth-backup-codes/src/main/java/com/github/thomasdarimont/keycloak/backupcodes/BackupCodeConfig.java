package com.github.thomasdarimont.keycloak.backupcodes;

import lombok.Data;
import org.keycloak.credential.hash.Pbkdf2PasswordHashProviderFactory;

// TODO make this configurable
@Data
public class BackupCodeConfig {

    int backupCodeCount = 10;
    int backupCodeLength = 8;
    int backupCodeHashIterations = 100;
    String hashingProviderId = Pbkdf2PasswordHashProviderFactory.ID;

}
