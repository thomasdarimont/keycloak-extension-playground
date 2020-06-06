package demo.keycloak.auth;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Map;

@JBossLog
public class ConditionalOnScopePresentAuthenticator implements ConditionalAuthenticator {

    static final ConditionalOnScopePresentAuthenticator SINGLETON = new ConditionalOnScopePresentAuthenticator();

    protected static final String CLIENT_SCOPE_NAME = "scope";

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {

        AuthenticatorConfigModel authConfig = context.getAuthenticatorConfig();
        if (authConfig == null) {
            return false;
        }

        Map<String, String> config = authConfig.getConfig();
        String requiredScopeName = config != null ? config.get(CLIENT_SCOPE_NAME) : null;

        ClientModel client = context.getSession().getContext().getClient();
        Map<String, ClientScopeModel> clientScopes = client.getClientScopes(true, true);

        return clientScopes != null && clientScopes.containsKey(requiredScopeName);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Not used
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Not used
    }

    @Override
    public void close() {
        // Does nothing
    }
}
