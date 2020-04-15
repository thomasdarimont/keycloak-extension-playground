package com.github.thomasdarimont.keycloak.auth.accesspolicy;

import com.github.thomasdarimont.keycloak.auth.accesspolicy.support.AccessPolicy;
import com.github.thomasdarimont.keycloak.auth.accesspolicy.support.AccessPolicyParser;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

/**
 * Simple {@link Authenticator} that checks of the user has a given {@link RoleModel Role}.
 */
@JBossLog
public class AccessPolicyAuthenticator implements Authenticator {

    private final AccessPolicyParser accessPolicyParser;

    public AccessPolicyAuthenticator(AccessPolicyParser accessPolicyParser) {
        this.accessPolicyParser = accessPolicyParser;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();

        if (configModel == null) {
            context.attempted();
            return;
        }

        String accessPolicyJson = configModel.getConfig().get(AccessPolicyAuthenticatorFactory.ACCESS_POLICY);
        if (accessPolicyJson == null) {
            context.attempted();
            return;
        }

        AccessPolicy accessPolicy = accessPolicyParser.parse(accessPolicyJson);

        RealmModel realm = context.getRealm();
        ClientModel client = context.getAuthenticationSession().getClient();
        UserModel user = context.getUser();

        if (!accessPolicy.hasAccess(realm, user, client)) {

            log.debugf("Access denied because of access policy. realm=%s client=%s username=%s", realm.getName(), client.getClientId(), user.getUsername());
            context.getEvent().user(user);
            context.getEvent().error(Errors.NOT_ALLOWED);
            context.forkWithErrorMessage(new FormMessage(Messages.NO_ACCESS));
            return;
        }


        context.success();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
