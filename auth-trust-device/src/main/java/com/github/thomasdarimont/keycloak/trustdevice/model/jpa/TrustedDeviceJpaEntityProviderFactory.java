package com.github.thomasdarimont.keycloak.trustdevice.model.jpa;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(JpaEntityProviderFactory.class)
public class TrustedDeviceJpaEntityProviderFactory implements JpaEntityProviderFactory {

    private static final TrustedDeviceJpaEntityProvider INSTANCE = new TrustedDeviceJpaEntityProvider();

    @Override
    public JpaEntityProvider create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public String getId() {
        return TrustedDeviceJpaEntityProvider.ID;
    }
}
