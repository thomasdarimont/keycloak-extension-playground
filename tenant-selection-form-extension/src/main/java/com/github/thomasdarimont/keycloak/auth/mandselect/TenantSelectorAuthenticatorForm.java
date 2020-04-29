package com.github.thomasdarimont.keycloak.auth.mandselect;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class TenantSelectorAuthenticatorForm implements Authenticator {

    private static final Logger LOG = Logger.getLogger(TenantSelectorAuthenticatorForm.class);

    public TenantSelectorAuthenticatorForm() {
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        // Note that you can use the `session` to access Keycloaks services.

        Response response = context.form()
                .setAttribute("username", context.getUser().getUsername())
                .createForm("tenant-select-form.ftl");

        context.challenge(response);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String tenant = formData.getFirst("tenant");
        String group = formData.getFirst("group");

        LOG.infof("Retrieved tenant=%s group=%s", tenant, group);

        if (group == null || tenant == null || group.trim().isEmpty() || tenant.trim().isEmpty()) {

            context.cancelLogin();

            // reauthenticate...
            authenticate(context);
            return;
        }

        // Add selected information to authentication session
        context.getAuthenticationSession().setUserSessionNote("tenant", tenant);
        context.getAuthenticationSession().setUserSessionNote("group", group);

        context.success();
    }

    @Override
    public void close() {
        // NOOP
    }
}
