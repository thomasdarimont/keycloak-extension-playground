package com.github.thomasdarimont.keycloak.auth.requirerole;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.RoleUtils;

import java.util.Set;

import static org.keycloak.models.utils.KeycloakModelUtils.getRoleFromString;

/**
 * Simple {@link Authenticator} that checks of the user has a given {@link RoleModel Role}.
 */
public class RequireRoleAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(RequireRoleAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();

        String roleName = configModel.getConfig().get(RequireRoleAuthenticatorFactory.ROLE);
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        if (!userHasRole(realm, user, roleName)) {

            LOG.debugf("Access denied because of missing role. realm=%s username=%s role=%s", realm.getName(), user.getUsername(), roleName);
            context.cancelLogin();
            return;
        }

        context.success();
    }

    /**
     * @param realm
     * @param user
     * @param roleName
     * @return true if roleName is in any of all user role mappings including all groups of user
     */
    private boolean userHasRole(RealmModel realm, UserModel user, String roleName) {

        if (roleName == null) {
            return false;
        }

        LOG.debugf("Checking if user=%s has role=%s", user.getUsername(), roleName);
        RoleModel requiredRole = getRoleFromString(realm, roleName);

        // first perform cheap role check
        Set<RoleModel> directAssignedRoles = user.getRoleMappings();
        if (RoleUtils.hasRole(directAssignedRoles, requiredRole)) {
            return true;
        }

        // Next perform more expensive nested roles check
        Set<RoleModel> nestedAssignedRoles = RoleUtils.getDeepUserRoleMappings(user);
        if (RoleUtils.hasRole(nestedAssignedRoles, requiredRole)) {
            return true;
        }

        LOG.debugf("User does not have the required role. user=%s role=%s assignedRoles=%s", user.getUsername(), requiredRole, nestedAssignedRoles);
        return false;
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
