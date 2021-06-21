package com.github.thomasdarimont.keycloak.backupcodes.auth;

import com.github.thomasdarimont.keycloak.backupcodes.credentials.BackupCodeCredentialModel;
import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.Collections;
import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class BackupCodeAuthenticatorFactory implements AuthenticatorFactory {

    private static final BackupCodeAuthenticator INSTANCE = new BackupCodeAuthenticator();

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        List<ProviderConfigProperty> list = ProviderConfigurationBuilder
                .create()
// TODO figure out how to access provider configuration in isConfiguredFor
//                .property().name("secondFactorRequired")
//                .type(ProviderConfigProperty.STRING_TYPE)
//                .label("Required Second Factor Credential Type")
//                .defaultValue(OTPCredentialModel.TYPE)
//                .helpText("If the credential model type is configured for the user the authenticator is offered." +
//                        "If the value is empty the authenticator is always offered.")
//                .add()
                .build();

        CONFIG_PROPERTIES = Collections.unmodifiableList(list);
    }

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
        return "Backup Codes for 2FA Recovery";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getReferenceCategory() {
        return BackupCodeCredentialModel.TYPE;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}