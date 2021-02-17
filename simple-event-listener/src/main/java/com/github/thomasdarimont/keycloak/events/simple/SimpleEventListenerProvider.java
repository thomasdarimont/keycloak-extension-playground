package com.github.thomasdarimont.keycloak.events.simple;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.actiontoken.execactions.ExecuteActionsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.LoginActionsService;

import javax.ws.rs.core.UriBuilder;
import java.util.List;

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

        if (event.getType() == EventType.UPDATE_PROFILE) {
            System.out.println("update  profile");

            //sendResetPasswordLinkOnProfileUpdate(event, user);
        }
    }

    private void sendResetPasswordLinkOnProfileUpdate(Event event, UserModel user) {

        RealmModel realm = session.realms().getRealm(event.getRealmId());
        int lifespan = realm.getActionTokenGeneratedByAdminLifespan();
        int expiration = Time.currentTime() + lifespan;

        String clientId = "account";
        String redirectUri = Urls.accountPage(session.getContext().getUri().getBaseUri(), realm.getName()).toString();

        ExecuteActionsActionToken token = new ExecuteActionsActionToken(user.getId(), expiration, List.of(UserModel.RequiredAction.UPDATE_PASSWORD.name()), redirectUri, clientId);

        UriBuilder builder = LoginActionsService.actionTokenProcessor(session.getContext().getUri());
        builder.queryParam("key", token.serialize(session, realm, session.getContext().getUri()));

        String link = builder.build(realm.getName()).toString();
        System.out.println("Link: " + link);
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
