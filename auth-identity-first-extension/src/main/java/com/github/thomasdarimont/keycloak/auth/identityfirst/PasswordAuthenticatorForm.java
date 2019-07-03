package com.github.thomasdarimont.keycloak.auth.identityfirst;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.credential.CredentialInput;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

public class PasswordAuthenticatorForm extends AbstractIdentityFirstUsernameFormAuthenticator {

    private static final Logger LOG = Logger.getLogger(PasswordAuthenticatorForm.class);
    private final KeycloakSession session;

    public PasswordAuthenticatorForm(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        Response response = challenge(context, null);

        context.challenge(response);
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, String error) {

        LoginFormsProvider form = context.form();

        if (error != null) {
            form.setError(error);
        }

        String attemptedUsername = context.getAuthenticationSession().getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);
        form.setAttribute(AuthenticationManager.FORM_USERNAME, attemptedUsername);

        Response response = form.createForm("validate-password-form.ftl");
        return response;
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
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            context.resetFlow();
            return;
        }
        if (!validatePasswordForm(context, formData)) {
            return;
        }

        context.success();
    }

    private boolean validatePasswordForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {

        String username = context.getAuthenticationSession().getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);
        UserModel user = lookupUser(context, username);
        if (username == null || user == null) {
            failWithUserNotFound(context);
            return false;
        }

        String password = formData.getFirst(CredentialRepresentation.PASSWORD);
        if (password == null || password.isEmpty()) {
            failWithInvalidCredentials(context, null);
            return false;
        }

        List<CredentialInput> credentials = new LinkedList<>();
        credentials.add(UserCredentialModel.password(password));


        if (isTemporarilyDisabledByBruteForce(context, user)) {
            return false;
        }

        if (!context.getSession().userCredentialManager().isValid(context.getRealm(), user, credentials)) {
            failWithInvalidCredentials(context, user);
            return false;
        }

        context.setUser(user);

        return true;
    }

    private void failWithInvalidCredentials(AuthenticationFlowContext context, UserModel user) {
        context.getEvent().user(user);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
        Response challengeResponse = challenge(context, Messages.INVALID_USER);
        context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
        context.clearUser();
    }

    @Override
    public void close() {
        // NOOP
    }
}
