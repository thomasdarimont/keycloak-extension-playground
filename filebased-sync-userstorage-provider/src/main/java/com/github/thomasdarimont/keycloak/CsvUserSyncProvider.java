package com.github.thomasdarimont.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.ImportSynchronization;
import org.keycloak.storage.user.SynchronizationResult;

import java.util.Date;

public class CsvUserSyncProvider implements UserStorageProvider, ImportSynchronization {

    public static String ID = "csv-user-sync-provider";
    private final KeycloakSession session;
    private final ComponentModel model;

    public CsvUserSyncProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public SynchronizationResult sync(KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        return syncSince(null, sessionFactory, realmId, model);
    }

    @Override
    public SynchronizationResult syncSince(Date lastSync, KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {

        SynchronizationResult result = new SynchronizationResult();

        return result;
    }
}
