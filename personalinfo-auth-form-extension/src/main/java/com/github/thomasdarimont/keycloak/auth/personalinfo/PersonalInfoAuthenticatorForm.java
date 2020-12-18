package com.github.thomasdarimont.keycloak.auth.personalinfo;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class PersonalInfoAuthenticatorForm implements Authenticator {

    private final KeycloakSession session;

    public PersonalInfoAuthenticatorForm(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        LoginFormsProvider form = context.form();
        Response response = form.createForm("personalinfo-form.ftl");

        context.challenge(response);
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        boolean matches = personalInfoMatches(formData, context.getUser());

        if (!matches) {
            LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
            form.setError(Messages.INVALID_USER);

            Response challengeResponse = form.createLoginUsernamePassword();
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);

            return;
        }

        context.success();
    }

    private boolean personalInfoMatches(MultivaluedMap<String, String> formData, UserModel user) {

        if (user == null) {
            return false;
        }

        String firstName = formData.getFirst("firstName");
        String lastName = formData.getFirst("lastName");

        return user.getFirstName().equals(firstName)
                && user.getLastName().equals(lastName);
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
    public void close() {
        // NOOP
    }
}
