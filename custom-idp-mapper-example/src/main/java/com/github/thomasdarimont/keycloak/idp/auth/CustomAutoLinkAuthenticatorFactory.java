package com.github.thomasdarimont.keycloak.idp.auth;

import com.google.auto.service.AutoService;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.broker.IdpAutoLinkAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

@AutoService(AuthenticatorFactory.class)
public class CustomAutoLinkAuthenticatorFactory extends IdpAutoLinkAuthenticatorFactory {

    private static final CustomAutoLinkAuthenticator INSTANCE = new CustomAutoLinkAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public String getReferenceCategory() {
        return "custom-autoLink";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public String getDisplayType() {
        return "Custom Automatically set existing user";
    }

    @Override
    public String getHelpText() {
        return "Automatically set existing user to authentication context without any verification";
    }
}
