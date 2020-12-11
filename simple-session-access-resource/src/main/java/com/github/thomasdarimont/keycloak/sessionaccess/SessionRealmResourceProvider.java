package com.github.thomasdarimont.keycloak.sessionaccess;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class SessionRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public SessionRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new SessionRealmResource(session);
    }

    @Override
    public void close() {

    }
}
