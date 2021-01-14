package com.github.thomasdarimont.keycloak.provisioning;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JBossLog
public class ProvisioningEventListener implements EventListenerProvider {

    static final String ID = "demo-provisioning-event-listener";

    private static final Set<EventType> SUPPORTED_USER_EVENT_TYPES = Set.of(EventType.REGISTER);
    private final KeycloakSession session;

    public ProvisioningEventListener(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {

        if (!SUPPORTED_USER_EVENT_TYPES.contains(event.getType())) {
            return;
        }

        log.infof("onEvent event=%s type=%s realm=%suserId=%s", event, event.getType(), event.getRealmId(), event.getUserId());

        if (event.getType() == EventType.REGISTER) {
            // user was created via self-registration or identity-brokering
            trySendWelcomeEmail(event.getRealmId(), event.getUserId());
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // log.infof("onEvent adminEvent=%s type=%s resourceType=%s resourcePath=%s includeRepresentation=%s", event, event.getOperationType(), event.getResourceType(), event.getResourcePath(), includeRepresentation);
        // NOOP

        if (event.getResourceType() == ResourceType.USER && event.getOperationType() == OperationType.CREATE) {
            // user was created via admin-console or admin-rest API
            trySendWelcomeEmail(event.getRealmId(), event.getResourcePath().substring("users/".length()));
        }
    }


    private void trySendWelcomeEmail(String realmId, String userId) {

        RealmModel realm = session.realms().getRealm(realmId);
        UserModel user = session.users().getUserById(userId, realm);

        if (user.getEmail() == null) {
            log.warnf("Could not send welcome email due to missing email. realm=%s user=%s", realm.getId(), user.getUsername());
            return;
        }

        UriBuilder authUriBuilder = UriBuilder.fromUri(session.getContext().getUri().getBaseUri());

        Map<String, Object> mailBodyAttributes = new HashMap<>();
        mailBodyAttributes.put("baseUri", authUriBuilder.replacePath("/auth").build());
        mailBodyAttributes.put("username", user.getUsername());

        String realmName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName();
        List<Object> subjectParams = List.of(realmName);

        try {
            EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class);
            emailProvider.setRealm(realm);
            emailProvider.setUser(user);
            // Don't forget to add the welcome-email.ftl (html and text) template to your theme.
            emailProvider.send("welcomeEmailSubject", subjectParams, "welcome-email.ftl", mailBodyAttributes);
        } catch (EmailException eex) {
            log.errorf(eex, "Failed to send welcome email. realm=%s user=%s", realm.getId(), user.getUsername());
        }
    }

    @Override
    public void close() {
        // NOOP
    }
}
