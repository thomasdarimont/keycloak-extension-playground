package keycloak.auth;

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
public class ThirdPartyMfaAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-3rd-party-mfa";

    @Override
    public String getDisplayType() {
        return "Third-party MFA Authenticator";
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
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
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
        return "Authenticator that supports MFA with a third-party service.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder
                .create()
                .property().name("customRole")
                .type(ProviderConfigProperty.ROLE_TYPE)
                .label("Some Role")
                .defaultValue("none")
                .helpText("Select some Role")
                .add()
                .property().name("otherProp")
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Some value")
                .defaultValue("test")
                .helpText("Enter some value")
                .add().build();
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {

        MfaClient mfaClient = new MockMfaClient();

        return new ThirdPartyMfaAuthenticator(session, mfaClient);
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
