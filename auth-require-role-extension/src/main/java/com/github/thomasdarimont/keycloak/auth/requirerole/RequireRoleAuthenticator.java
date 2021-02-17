package com.github.thomasdarimont.keycloak.auth.requirerole;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.RoleUtils;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

import static org.keycloak.models.utils.KeycloakModelUtils.getRoleFromString;

/**
 * Simple {@link Authenticator} that checks of the user has a given {@link RoleModel Role}.
 */
public class RequireRoleAuthenticator implements Authenticator {

    protected static final Logger LOG = Logger.getLogger(RequireRoleAuthenticator.class);

    public static final String CLIENT_ID_PLACEHOLDER = "${clientId}";

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        ClientModel client = context.getAuthenticationSession().getClient();
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();

        String roleName = resolveRoleName(authenticatorConfig.getConfig(), client);
        if (roleName == null) {
            context.success();
            return;
        }

        RoleModel requiredRole = resolveRequiredRole(roleName, realm, client);
        if (requiredRole == null) {
            context.success();
            return;
        }

        if (isUserInRole(user, requiredRole)) {
            context.success();
            return;
        }

        LOG.debugf("Access denied because of missing role. realm=%s username=%s role=%s", realm.getName(), user.getUsername(), roleName);
        context.getEvent().user(user);
        context.getEvent().error(Errors.NOT_ALLOWED);

        // TODO make fallback client configurable
        // ClientModel fallbackClientForBacklink = realm.getClientByClientId("account");

        LoginFormsProvider loginFormsProvider = context.form();
        /* TODO set an attribute here to allow overriding fallback client URL.
            Note that this requires a custom a custom error.ftl.
         */

        Response errorForm = loginFormsProvider
                .setError("Access Denied: " + client.getClientId())
                .createErrorPage(Response.Status.FORBIDDEN);

        context.forceChallenge(errorForm);
    }

    protected RoleModel resolveRequiredRole(String roleName, RealmModel realm, ClientModel client) {
        return getRoleFromString(realm, roleName);
    }

    protected String resolveRoleName(Map<String, String> config, ClientModel client) {

        if (config == null) {
            return null;
        }

        String roleName = config.get(RequireRoleAuthenticatorFactory.ROLE);
        if (roleName == null) {
            return null;
        }

        roleName = roleName.trim();

        if (roleName.isBlank()) {
            return null;
        }

        if (roleName.startsWith(CLIENT_ID_PLACEHOLDER)) {
            roleName = roleName.replace(CLIENT_ID_PLACEHOLDER, client.getClientId());
        }

        return roleName;
    }

    /**
     * @param user
     * @param requiredRole
     * @return true if requiredRole is in any of all user role mappings including all groups of user
     */
    protected boolean isUserInRole(UserModel user, RoleModel requiredRole) {

        if (requiredRole == null) {
            return true;
        }

        LOG.debugf("Checking if user=%s has role=%s", user.getUsername(), requiredRole.getName());

        // First perform cheap role check for direct or composite roles
        if (RoleUtils.hasRole(user.getRoleMappingsStream(), requiredRole)) {
            return true;
        }

        // Next perform more expensive roles check for group membership role mappings
        Set<RoleModel> nestedAssignedRoles = RoleUtils.getDeepUserRoleMappings(user);
        if (RoleUtils.hasRole(nestedAssignedRoles, requiredRole)) {
            return true;
        }

        LOG.debugf("User does not have the required role. user=%s role=%s assignedRoles=%s", user.getUsername(), requiredRole, nestedAssignedRoles);
        return false;
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
