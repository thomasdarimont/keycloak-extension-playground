package com.github.thomasdarimont.keycloak.auth.requirerole;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Simple {@link Authenticator} that checks if the user has a given {@link RoleModel Role} and return correct error for usage in direct grant flow.
 */
public class RequireRoleDirectGrantAuthenticator extends RequireRoleAuthenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();

        String roleName = configModel.getConfig().get(RequireRoleDirectGrantAuthenticatorFactory.ROLE);
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        if (userHasRole(realm, user, roleName)) {
            context.success();
            return;
        }

        LOG.debugf("Access denied because of missing role. realm=%s username=%s role=%s", realm.getName(), user.getUsername(), roleName);
        String responsePhrase = String.format("Access denied because of missing role. realm=%s username=%s role=%s", realm.getName(), user.getUsername(), roleName);

        Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "missing_role", responsePhrase);
        context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
    }

    private Response errorResponse(int status, String error, String errorDescription) {
        OAuth2ErrorRepresentation errorRep = new OAuth2ErrorRepresentation(error, errorDescription);
        return Response.status(status).entity(errorRep).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
