package demo.keycloak.passwordpolicy;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;

@AutoService(PasswordPolicyProviderFactory.class)
public class CustomPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {

    @Override
    public String getId() {
        return "custom-pwd-policy";
    }

    @Override
    public String getDisplayName() {
        return "Custom Password Policy Provider";
    }

    @Override
    public String getConfigType() {
        return null;
    }

    @Override
    public String getDefaultConfigValue() {
        return null;
    }

    @Override
    public boolean isMultiplSupported() {
        return false;
    }

    @Override
    public PasswordPolicyProvider create(KeycloakSession session) {
        return new CustomPasswordPolicyProvider(session);
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
