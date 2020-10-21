package com.github.thomasdarimont.keycloak.userstorage.flyweight;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.federated.UserAttributeFederatedStorage;
import org.keycloak.storage.federated.UserRoleMappingsFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JBossLog
public class FlyweightAcmeUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        UserQueryProvider,

        UserAttributeFederatedStorage,

        CredentialInputUpdater,
        CredentialInputValidator,

        UserRoleMappingsFederatedStorage {

    private final KeycloakSession session;
    private final ComponentModel storageComponentModel;
    private final AcmeUserRepository repository;

    public FlyweightAcmeUserStorageProvider(KeycloakSession session, ComponentModel storageComponentModel, AcmeUserRepository repository) {
        this.session = session;
        this.storageComponentModel = storageComponentModel;
        this.repository = repository;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

        log.infov("isValid user credential: userId={0}", user.getId());

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) input;
        return repository.validateCredentials(user.getUsername(), cred.getValue());
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {

        log.infov("updating credential: realm={0} user={1}", realm.getId(), user.getUsername());

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) input;
        return repository.updateCredentials(user.getUsername(), cred.getValue());
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        log.infov("disable credential type: realm={0} user={1} credentialType={2}", realm.getId(), user.getUsername(), credentialType);
    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        return Collections.emptySet();
    }

    @Override
    public void preRemove(RealmModel realm) {

        log.infov("pre-remove realm");
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {

        log.infov("pre-remove group");
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {

        log.infov("pre-remove role");
    }

    @Override
    public void close() {
        log.infov("closing");
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {

        log.infov("lookup user by id: realm={0} userId={1}", realm.getId(), id);

        String externalId = StorageId.externalId(id);
        return createAdapter(realm, repository.findUserById(externalId));
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {

        log.infov("lookup user by username: realm={0} username={1}", realm.getId(), username);
        return createAdapter(realm, repository.findUserByUsernameOrEmail(username));
    }

    protected UserModel createAdapter(RealmModel realm, AcmeUser acmeUser) {

        AcmeUserAdapter acmeUserAdapter = new AcmeUserAdapter(session, realm, storageComponentModel, acmeUser);
        return acmeUserAdapter;
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {

        log.infov("lookup user by username: realm={0} email={1}", realm.getId(), email);

        return getUserByUsername(email, realm);
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return repository.getUsersCount();
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {

        log.infov("list users: realm={0}", realm.getId());

        return repository.getAllUsers().stream()
                .map(acmeUser -> new AcmeUserAdapter(session, realm, storageComponentModel, acmeUser))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {

        log.infov("list users: realm={0} firstResult={1} maxResults={2}", realm.getId(), firstResult, maxResults);

        return getUsers(realm);
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {

        log.infov("search for users: realm={0} search={1}", realm.getId(), search);

        return repository.findUsers(search).stream()
                .map(acmeUser -> new AcmeUserAdapter(session, realm, storageComponentModel, acmeUser))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {

        log.infov("search for users: realm={0} search={1} firstResult={2} maxResults={3}", realm.getId(), search, firstResult, maxResults);

        return searchForUser(search, realm);
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {

        log.infov("search for users with params: realm={0} params={1}", realm.getId(), params);

        return searchForUser("", realm);
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {

        log.infov("search for users with params: realm={0} params={1} firstResult={2} maxResults={3}", realm.getId(), params, firstResult, maxResults);

        return searchForUser("", realm);
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {

        log.infov("search for group members with params: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(), group.getId(), firstResult, maxResults);

        return Collections.emptyList();
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {

        log.infov("search for group members: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(), group.getId());

        return Collections.emptyList();
    }

    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {

        log.infov("search for group members: realm={0} attrName={1} attrValue={2}", realm.getId(), attrName, attrValue);

        return null;
    }

    /* UserRoleMappingsFederatedStorage start */
    @Override
    public void grantRole(RealmModel realm, String userId, RoleModel role) {
        log.infov("grant role mapping: realm={0} userId={1} role={2}", realm.getId(), userId, role.getName());
    }

    @Override
    public Set<RoleModel> getRoleMappings(RealmModel realm, String userId) {
        log.infov("get role mappings: realm={0} userId={1}", realm.getId(), userId);

        String externalUserId = StorageId.externalId(userId);

        Set<AcmeRole> roles = repository.getGlobalRolesByUserId(externalUserId);
        Set<RoleModel> externalRoles = roles.stream()
                .map(role -> new AcmeRoleModel(role.getId(), role.getName(), role.getDescription(), false, realm))
                .collect(Collectors.toSet());

        for (ClientModel client : realm.getClients()) {

            String clientId = client.getClientId();
            // potentially filter for acme clients...

            Set<AcmeRole> clientRolesByUserId = repository.getClientRolesByUserId(clientId, externalUserId);
            if (clientRolesByUserId != null) {
                Set<RoleModel> externalClientRoles = clientRolesByUserId.stream()
                        .map(role -> new AcmeRoleModel(role.getId(), role.getName(), role.getDescription(), false, client))
                        .collect(Collectors.toSet());
                externalRoles.addAll(externalClientRoles);
            }
        }

        return externalRoles;
    }

    @Override
    public void deleteRoleMapping(RealmModel realm, String userId, RoleModel role) {
        log.infov("delete role mapping: realm={0} userId={1} role={2}", realm.getId(), userId, role.getName());
    }

    /* UserRoleMappingsFederatedStorage end */

    @Override
    public void setSingleAttribute(RealmModel realm, String userId, String name, String value) {
        log.infov("set single attribute: realm={0} userId={1} name={2} value={3}", realm.getId(), userId, name, value);
    }

    @Override
    public void setAttribute(RealmModel realm, String userId, String name, List<String> values) {
        log.infov("set attribute: realm={0} userId={1} name={2} value={3}", realm.getId(), userId, name, values);
    }

    @Override
    public void removeAttribute(RealmModel realm, String userId, String name) {
        log.infov("remove attribute: realm={0} userId={1} name={2}", realm.getId(), userId, name);
    }

    @Override
    public MultivaluedHashMap<String, String> getAttributes(RealmModel realm, String userId) {

        log.infov("get attributes: realm={0} userId={1}", realm.getId(), userId);

        String externalId = StorageId.externalId(userId);
        AcmeUser acmeUser = repository.findUserById(externalId);

        return new MultivaluedHashMap<>(acmeUser.getAttributes());
    }

    @Override
    public List<String> getUsersByUserAttribute(RealmModel realm, String name, String value) {

        log.infov("get users by user attribute: realm={0} name={1} value={2}", realm.getId(), value);

        return List.of();
    }
}
