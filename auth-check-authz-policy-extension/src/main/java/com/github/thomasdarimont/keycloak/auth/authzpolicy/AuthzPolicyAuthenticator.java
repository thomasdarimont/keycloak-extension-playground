package com.github.thomasdarimont.keycloak.auth.authzpolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthzPolicyAuthenticator implements Authenticator {

    private static final ObjectMapper OM = new ObjectMapper();

    static final String ROLES_POLICY = "rolesPolicy";

    static final String CLIENTS_POLICY = "clientsPolicy";

    private final KeycloakSession session;

    public AuthzPolicyAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        RealmModel realm = context.getRealm();
        ClientModel client = context.getAuthenticationSession().getClient();

        AuthorizationProvider authzProvider = session.getProvider(AuthorizationProvider.class);
        PolicyStore policyStore = authzProvider.getStoreFactory().getPolicyStore();

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        Map<String, String> config = configModel.getConfig();

        String clientPolicyName = config.get(CLIENTS_POLICY);
        String rolePolicyName = config.get(ROLES_POLICY);

        String realmManagementClientId = realm.getClientByClientId(Constants.REALM_MANAGEMENT_CLIENT_ID).getId();
        Policy clientPolicy = policyStore.findByName(clientPolicyName, realmManagementClientId);

        List<String> clients = parseJson(clientPolicy.getConfig().get("clients"), List.class);
        if (!clients.contains(client.getId())) {
            // The current client is not contained in the client policy -> skip the authenticator
            context.success();
            return;
        }

        Policy rolePolicy = policyStore.findByName(rolePolicyName, realmManagementClientId);
        List<Map<String, Object>> roles = parseJson(rolePolicy.getConfig().get("roles"), List.class);
        List<RoleModel> requiredRoles = roles.stream()
                .map(r -> (String) r.get("id"))
                .map(realm::getRoleById)
                .collect(Collectors.toList());

        UserModel user = context.getUser();
        boolean accessAllowed = requiredRoles.stream().anyMatch(user::hasRole);

        if (accessAllowed) {
            // the user has the required roles -> let the authentication succeed
            context.success();
            return;
        }

        // the user does not have the required roles -> deny the authentication

        context.getEvent().user(user);
        context.getEvent().error(Errors.NOT_ALLOWED);
        context.forkWithErrorMessage(new FormMessage(Messages.NO_ACCESS));
    }

    private <T> T parseJson(String json, Class<T> type) {
        try {
            return OM.readValue(json, type);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP
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
