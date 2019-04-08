package com.github.thomasdarimont.keycloak.simple;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class SimpleRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public SimpleRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new SimpleRealmResource(session);
    }

    @Override
    public void close() {

    }
}
