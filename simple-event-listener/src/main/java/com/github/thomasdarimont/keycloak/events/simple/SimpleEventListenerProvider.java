package com.github.thomasdarimont.keycloak.events.simple;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

@JBossLog
public class SimpleEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public SimpleEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        log.infof("onEvent event={}", event);
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        log.infof("onEvent adminEvent={}, includeRepresentation=", event, includeRepresentation);
    }

    @Override
    public void close() {
        log.infof("close");
    }
}
