package com.github.thomasdarimont.keycloak;

import com.google.auto.service.AutoService;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

@AutoService(UserStorageProviderFactory.class)
public class CsvUserSyncProviderFactory implements UserStorageProviderFactory<CsvUserSyncProvider> {

    @Override
    public CsvUserSyncProvider create(KeycloakSession session, ComponentModel model) {
        return new CsvUserSyncProvider(session, model);
    }

    @Override
    public String getId() {
        return CsvUserSyncProvider.ID;
    }
}
