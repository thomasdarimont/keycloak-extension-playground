package com.github.thomasdarimont.keycloak.auth.requiregroup;

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

        LOG.debugf("user=%s has role=%s. Access granted", user.getUsername(), roleName);
        context.success();
    }

   /**
     *
     * @param realm
     * @param user
     * @param roleName
     * @return true if roleName is in any of all user role mappings including all groups of user
     */
    private boolean userHasRole(RealmModel realm, UserModel user, String roleName) {
        LOG.debugf("Checking if user=%s has role=%s or inherits it from any of its groups...", user.getUsername(), roleName);

        if (roleName == null) {
            LOG.debugf("Required role name is empty", user.getUsername(), roleName);
            return false;
        }

        RoleModel role = getRoleFromString(realm, roleName);
        Set<RoleModel> roles = RoleUtils.getDeepUserRoleMappings(user);
        LOG.debugf("user=%s has assigned this list of group roles=%s, the required one is: %s", user.getUsername(), roles, role);
        return (roles.contains(role))?true:false;
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
