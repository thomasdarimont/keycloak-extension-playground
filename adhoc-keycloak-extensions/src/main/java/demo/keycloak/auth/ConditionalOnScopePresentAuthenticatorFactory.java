package demo.keycloak.auth;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.Collections;
import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class ConditionalOnScopePresentAuthenticatorFactory implements ConditionalAuthenticatorFactory {

    public static final String PROVIDER_ID = "demo-conditional-scope-present";


    private static final List<ProviderConfigProperty> providerConfigProperties;

    static {
        List<ProviderConfigProperty> list = ProviderConfigurationBuilder.create()
                .property()
                .name(ConditionalOnScopePresentAuthenticator.CLIENT_SCOPE_NAME)
                .label("Scope Name")
                .helpText("The name of the client scope that must be present")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .build();

        providerConfigProperties = Collections.unmodifiableList(list);
    }

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Condition - Client Scope present";
    }

    @Override
    public String getReferenceCategory() {
        return "condition";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
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
        return "Flow is executed only if client has required scope configured.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return providerConfigProperties;
    }

    @Override
    public ConditionalAuthenticator getSingleton() {
        return ConditionalOnScopePresentAuthenticator.SINGLETON;
    }
}
