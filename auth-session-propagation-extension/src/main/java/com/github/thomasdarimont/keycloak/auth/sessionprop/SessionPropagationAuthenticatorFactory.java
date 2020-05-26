package com.github.thomasdarimont.keycloak.auth.sessionprop;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class SessionPropagationAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-user-session-propagation";

    @Override
    public String getDisplayType() {
        return "Demo: Propragate user session";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.ALTERNATIVE, AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Propagates an externally created login session into the browser.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder
                .create()
                .property().name(SessionPropagationAuthenticator.ENCRYPTION_KEY)
                .type(ProviderConfigProperty.PASSWORD)
                .label("Encryption Key")
                .defaultValue("changeme")
                .helpText("Encryption key")
                .add()
                .property().name(SessionPropagationAuthenticator.SESSION_REFERENCE_MAX_AGE_SECONDS)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Session Reference Mag Age")
                .defaultValue("30")
                .helpText("Maximum age of session reference in seconds")
                .add().property().name(SessionPropagationAuthenticator.SESSION_VALIDATION_SERVICE_URL)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Session Validation URL")
                .defaultValue("")
                .helpText("Url to validate the encrypted session token against. " +
                        "The URI placeholder {sessionHandle} will be replaced witht he actual sessionHandle. " +
                        "An example URI can look like this: http://myserver/myapp/sessions/keycloak?sessionHandle={sessionHandle}")
                .add().build();
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new SessionPropagationAuthenticator(session);
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
    public String getId() {
        return PROVIDER_ID;
    }
}
