package com.github.thomasdarimont.keycloak.motd;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Map;

public class MotdAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        LoginFormsProvider form = context.form();

        MotdMessageProvider motdProvider = new SimpleMotdMessageProvider();
        MotdMessage message = motdProvider.getMessage(context.getSession(), getConfig(context));

        form.setAttribute("messageHeader", message.getHeader());
        form.setInfo(message.getDetails());
        // create proceed link
        form.setAttribute("actionUri", context.getActionUrl(context.generateAccessCode()));

        context.challenge(form.createInfoPage());
    }

    protected Map<String, String> getConfig(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        if (configModel == null) {
            return null;
        }
        return configModel.getConfig();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // proceed with the authentication
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
    public void close() {
        // NOOP
    }
}
