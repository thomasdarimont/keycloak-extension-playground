package com.github.thomasdarimont.keycloak.events.simple;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.utils.RoleUtils;

import static org.keycloak.models.utils.KeycloakModelUtils.getRoleFromString;

@JBossLog
public class UnsetPasswordEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public UnsetPasswordEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {

        if (!EventType.LOGIN.equals(event.getType())) {
            return;
        }

        log.infof("onEvent: event=%s", event.getType());

        UserProvider users = this.session.getProvider(UserProvider.class);
        RealmModel realm = session.getContext().getRealm();
        UserModel user = users.getUserById(event.getUserId(), realm);

        if (userHasRole(realm, user, "admin")) {
            return;
        }

        log.infof("onEvent: event=%s disable password credentials", event.getType());
        UserCredentialManager credentialManager = session.userCredentialManager();
        credentialManager.disableCredentialType(realm, user, CredentialModel.PASSWORD);

        UserCache userCache = session.getProvider(UserCache.class);
        userCache.evict(realm, user);
    }

    private boolean userHasRole(RealmModel realm, UserModel user, String roleName) {

        if (roleName == null) {
            return false;
        }

        RoleModel role = getRoleFromString(realm, roleName);

        return RoleUtils.hasRole(user.getRoleMappings(), role);
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
