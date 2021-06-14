package com.github.thomasdarimont.keycloak.backupcodes.credentials;

import com.google.auto.service.AutoService;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

@AutoService(CredentialProviderFactory.class)
public class BackupCodeCredentialProviderFactory implements CredentialProviderFactory<BackupCodeCredentialProvider> {

    // TODO consider changing this to "backup-code"
    public static final String ID = "custom-backup-code";

    @Override
    public CredentialProvider<CredentialModel> create(KeycloakSession session) {
        return new BackupCodeCredentialProvider(session);
    }

    @Override
    public String getId() {
        return ID;
    }
}
