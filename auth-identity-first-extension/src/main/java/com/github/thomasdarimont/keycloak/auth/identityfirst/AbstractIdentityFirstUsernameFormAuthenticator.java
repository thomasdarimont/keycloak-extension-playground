package com.github.thomasdarimont.keycloak.auth.identityfirst;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Errors;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.Response;

public abstract class AbstractIdentityFirstUsernameFormAuthenticator extends AbstractUsernameFormAuthenticator {


    protected UserModel lookupUser(AuthenticationFlowContext context, String username) {

        try {
            return KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);

            // Could happen during federation import
            if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
                setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS, AuthenticationFlowError.INVALID_USER);
            } else {
                setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS, AuthenticationFlowError.INVALID_USER);
            }
        }

        return null;
    }


    protected void failWithUserNotFound(AuthenticationFlowContext context) {
        context.getEvent().error(Errors.USER_NOT_FOUND);
        Response challengeResponse = challenge(context, Messages.INVALID_USER);
        context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
    }

}
