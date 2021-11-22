package com.github.thomasdarimont.keycloak.federation;

import com.github.thomasdarimont.keycloak.federation.client.KeycloakFacade;
import com.github.thomasdarimont.keycloak.federation.client.KeycloakFacadeProvider;
import com.github.thomasdarimont.keycloak.federation.client.SimpleKeycloakFacadeProvider;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.OAuth2Constants;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.InMemoryUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@JBossLog
public class KeycloakUserStorageProvider implements UserStorageProvider, //
        UserLookupProvider, // User lookup by username, email, userId
        CredentialInputValidator //  Credential validation
{

    public static final String EXTERNAL_USER_ID_ATTRIBUTE = "externalUserIdAttribute";
    private final KeycloakSession session;
    private final ComponentModel componentModel;
    private final ConcurrentMap<String, KeycloakFacadeProvider> keycloakFacadeProvider;

    public KeycloakUserStorageProvider(KeycloakSession session, ComponentModel componentModel, ConcurrentMap<String, KeycloakFacadeProvider> keycloakFacadeProvider) {
        this.session = session;
        this.componentModel = componentModel;
        this.keycloakFacadeProvider = keycloakFacadeProvider;
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {

        log.infof("getUserById id=%s realm=%s", id, realm.getName());

        String externalId = StorageId.externalId(id);
        UserRepresentation user;
        try {
            user = getKeycloakFacade().getUserById(componentModel.get("realm"), externalId);
        } catch (Exception ex) {
            return null;
        }

        if (user == null) {
            return null;
        }

        return toAdapter(realm, user);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {

        log.infof("getUserByUsername username=%s realm=%s", username, realm.getName());

        UserModel localUser = session.userLocalStorage().getUserByUsername(realm, username);
        if (localUser != null) {
            return localUser;
        }

        List<UserRepresentation> users;
        try {
            users = getKeycloakFacade().getUserByUsername(componentModel.get("realm"), username, false);
        } catch (Exception ex) {
            return null;
        }

        if (users == null || users.isEmpty()) {
            return null;
        }

        UserRepresentation user = users.get(0);
        return toAdapter(realm, user);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {

        log.infof("getUserByEmail email=%s realm=%s", email, realm.getName());

        UserModel localUser = session.userLocalStorage().getUserByEmail(realm, email);
        if (localUser != null) {
            return localUser;
        }

        List<UserRepresentation> users;
        try {
            users = getKeycloakFacade().getUserByEmail(componentModel.get("realm"), email, false);
        } catch (Exception ex) {
            return null;
        }

        if (users == null || users.isEmpty()) {
            return null;
        }

        UserRepresentation user = users.get(0);
        return toAdapter(realm, user);
    }

    private InMemoryUserAdapter toAdapter(RealmModel realm, UserRepresentation userRep) {

        String storageId = StorageId.keycloakId(componentModel, userRep.getId());
        InMemoryUserAdapter userAdapter = new InMemoryUserAdapter(session, realm, storageId);
        userAdapter.setFederationLink(componentModel.getId());
        userAdapter.setUsername(userRep.getUsername());
        userAdapter.setFirstName(userRep.getFirstName());
        userAdapter.setLastName(userRep.getLastName());
        userAdapter.setEmail(userRep.getEmail());
        userAdapter.setEmailVerified(userRep.isEmailVerified());
        userAdapter.setEnabled(userRep.isEnabled());

        collectDefaultRealmRoles(realm).forEach(userAdapter::grantRole);
        collectDefaultClientRoles(realm).forEach(userAdapter::grantRole);

        return userAdapter;
    }

    private List<RoleModel> collectDefaultRealmRoles(RealmModel realm) {

        String defaultRealmRolesToAdd = componentModel.get("defaultRealmRoles");
        if (defaultRealmRolesToAdd == null) {
            return Collections.emptyList();
        }

        List<RoleModel> realmRoles = new ArrayList<>();
        String[] realmRoleNames = defaultRealmRolesToAdd.split(",");
        for (String realmRole : realmRoleNames) {
            RoleModel role = realm.getRole(realmRole);
            if (role != null) {
                realmRoles.add(role);
            }
        }

        return realmRoles.isEmpty() ? Collections.emptyList() : realmRoles;
    }

    private List<RoleModel> collectDefaultClientRoles(RealmModel realm) {

        String defaultClientRolesToAdd = componentModel.get("defaultClientRoles");
        if (defaultClientRolesToAdd == null) {
            return Collections.emptyList();
        }


        String[] clientRoleMappings = defaultClientRolesToAdd.split(";");
        if (clientRoleMappings.length <= 0) {
            return Collections.emptyList();
        }

        List<RoleModel> clientRoles = new ArrayList<>();
        for (String clientRoleMapping : clientRoleMappings) {

            String[] clientIdAndRoles = clientRoleMapping.split("=");
            String clientId = clientIdAndRoles[0];
            ClientModel client = realm.getClientByClientId(clientId);

            if (client == null) {
                continue;
            }

            String[] clientRoleNames = clientIdAndRoles[1].split(",");
            if (clientRoleNames.length <= 0) {
                continue;
            }

            if ("*".equals(clientRoleNames[0])) {
                client.getRolesStream().collect(Collectors.toCollection(() -> clientRoles));
            } else {
                for (String clientRole : clientRoleNames) {
                    RoleModel role = client.getRole(clientRole);
                    if (role == null) {
                        continue;
                    }
                    clientRoles.add(role);
                }
            }
        }

        return clientRoles.isEmpty() ? Collections.emptyList() : clientRoles;
    }

    protected KeycloakFacadeProvider getKeycloakFacadeProvider() {

        KeycloakFacadeProvider keycloakFacade = keycloakFacadeProvider.computeIfAbsent(componentModel.getId(), storageProviderId -> {

            KeycloakFacadeProvider provider = createKeycloakFacadeProvider();
            return provider;
        });

        return keycloakFacade;
    }

    protected SimpleKeycloakFacadeProvider createKeycloakFacadeProvider() {
        return new SimpleKeycloakFacadeProvider(componentModel);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        String providerId = StorageId.providerId(user.getId());
        return componentModel.getId().equals(providerId);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {

        try {
            AccessTokenResponse accessTokenResponse = getKeycloakFacade().validatePassword( //
                    componentModel.get("realm"), //
                    componentModel.get("clientId"), //
                    componentModel.get("clientSecret"), //
                    user.getUsername(),  //
                    credentialInput.getChallengeResponse(), //
                    OAuth2Constants.PASSWORD, //
                    "profile email");

            AccessToken accessToken = getKeycloakFacadeProvider().verifyAccessToken(accessTokenResponse);
            if (accessToken == null) {
                return false;
            }

            onUserCredentialVerified(realm, user, accessToken, credentialInput);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected void onUserCredentialVerified(RealmModel realm, UserModel user, AccessToken accessToken, CredentialInput credentialInput) {

        boolean importEnabled = componentModel.get("importEnabled", false);
        if (!importEnabled) {
            return;
        }

        boolean useEmailAsUsername = componentModel.get("useEmailAsUsername", false);
        String username = useEmailAsUsername ? user.getEmail() : user.getUsername();

        UserModel localUser = session.userLocalStorage().addUser(realm, username);
        localUser.setEmail(user.getEmail());
        localUser.setEmailVerified(user.isEmailVerified());
        localUser.setFirstName(user.getFirstName());
        localUser.setLastName(user.getLastName());
        localUser.setEnabled(user.isEnabled());

        String externalUserIdAttribute = componentModel.get(EXTERNAL_USER_ID_ATTRIBUTE);
        localUser.setSingleAttribute(externalUserIdAttribute, accessToken.getSubject());

        try {
            session.userCredentialManager().updateCredential(realm, localUser, UserCredentialModel.password(credentialInput.getChallengeResponse(), false));
            log.info("Local User created with password");
        } catch (IllegalStateException ise) {
            throw new BadRequestException("Resetting to N old passwords is not allowed.");
        } catch (ReadOnlyException mre) {
            throw new BadRequestException("Can't reset password as account is read only");
        } catch (ModelException e) {
            log.warn("Could not update user password.", e);
            Properties messages = AdminRoot.getMessages(session, realm, "en_EN");
            throw new ErrorResponseException(e.getMessage(), MessageFormat.format(messages.getProperty(e.getMessage(), e.getMessage()), e.getParameters()), Response.Status.BAD_REQUEST);
        }
    }

    private KeycloakFacade getKeycloakFacade() {
        return getKeycloakFacadeProvider().getKeycloakFacade();
    }
}
