package demo.keycloak.passwordpolicy;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PolicyError;

public class CustomPasswordPolicyProvider implements PasswordPolicyProvider {

    private final KeycloakSession session;

    public CustomPasswordPolicyProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {

        System.out.println("Custom password validation");
        return null;
    }

    @Override
    public PolicyError validate(String user, String password) {
        return null;
    }

    @Override
    public Object parseConfig(String value) {
        return null;
    }

    @Override
    public void close() {
        // NOOP
    }
}
