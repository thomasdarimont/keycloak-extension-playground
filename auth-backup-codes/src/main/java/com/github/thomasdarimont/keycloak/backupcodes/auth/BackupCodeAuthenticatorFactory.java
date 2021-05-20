package com.github.thomasdarimont.keycloak.backupcodes.auth;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import com.google.auto.service.AutoService;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.PasswordFormFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class BackupCodeAuthenticatorFactory extends PasswordFormFactory {

    private static final BackupCodeAuthenticator INSTANCE = new BackupCodeAuthenticator();

    @Override
    public String getId() {
        return BackupCodeAuthenticator.ID;
    }

    @Override
    public String getDisplayType() {
        return "Backup Code Authenticator";
    }

    @Override
    public String getHelpText() {
        return "Backup Code Authenticator Help";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public String getReferenceCategory() {
        return BackupCode.CREDENTIAL_TYPE;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return INSTANCE;
    }
}