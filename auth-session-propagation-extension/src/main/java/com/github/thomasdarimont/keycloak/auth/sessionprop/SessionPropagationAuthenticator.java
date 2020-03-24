package com.github.thomasdarimont.keycloak.auth.sessionprop;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;

import javax.ws.rs.core.MultivaluedMap;

public class SessionPropagationAuthenticator implements Authenticator {

    private final KeycloakSession session;

    public SessionPropagationAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        // HACK: Don't do this at home! You need to at least encrypt the sessionIdentitifer and take care when
        // handling session ids. Otherwise users could take over sessions of other users.

        MultivaluedMap<String, String> queryParameters = context.getHttpRequest().getUri().getQueryParameters();
        String propSessionId = queryParameters.getFirst("propSessionId");
        if (propSessionId == null) {
            context.attempted();
            return;
        }

        RealmModel realm = context.getRealm();
        UserSessionModel userSession = session.sessions().getUserSession(realm, propSessionId);

        context.getAuthenticationSession().setAuthenticatedUser(userSession.getUser());

        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
