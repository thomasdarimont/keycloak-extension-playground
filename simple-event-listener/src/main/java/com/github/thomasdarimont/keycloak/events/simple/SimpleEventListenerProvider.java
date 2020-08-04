package com.github.thomasdarimont.keycloak.events.simple;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

@JBossLog
public class SimpleEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public SimpleEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        log.infof("onEvent event=%s type=%s realm=%suserId=%s", event, event.getType(), event.getRealmId(), event.getUserId());

        UserModel user = this.session.users().getUserById(event.getUserId(), session.realms().getRealm(event.getRealmId()));
        // user.getAttributes()
        // user.getFirstAttribute("attr")

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        log.infof("onEvent adminEvent=%s type=%s resourceType=%s resourcePath=%s includeRepresentation=%s", event, event.getOperationType(), event.getResourceType(), event.getResourcePath(), includeRepresentation);
    }

    @Override
    public void close() {
        // log.infof("close");
    }
}
