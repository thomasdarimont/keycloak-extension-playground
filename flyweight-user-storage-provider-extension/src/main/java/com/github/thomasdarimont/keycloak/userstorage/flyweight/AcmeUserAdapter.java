package com.github.thomasdarimont.keycloak.userstorage.flyweight;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

import java.util.Set;

public class AcmeUserAdapter extends AbstractUserAdapterFederatedStorage {

    private final AcmeUser acmeUser;
    private final String keycloakId;

    public AcmeUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, AcmeUser acmeUser) {
        super(session, realm, model);
        this.acmeUser = acmeUser;
        this.keycloakId = StorageId.keycloakId(model, acmeUser.getId());
    }

    @Override
    public UserFederatedStorageProvider getFederatedStorage() {
        // internal JPA user storage in keycloak.
        // used to store data that cannot be stored by the external federation provider
        return super.getFederatedStorage();
    }

    protected FlyweightAcmeUserStorageProvider getExternalUserStorageProvider() {
        // external user storage provider
        return session.getProvider(FlyweightAcmeUserStorageProvider.class, this.storageProviderModel);
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public String getUsername() {
        return acmeUser.getUsername();
    }

    @Override
    public void setUsername(String username) {
        acmeUser.setUsername(username);
    }

    @Override
    public String getEmail() {
        return acmeUser.getEmail();
    }

    @Override
    public void setEmail(String email) {
        acmeUser.setEmail(email);
    }

    @Override
    public String getFirstName() {
        return acmeUser.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        acmeUser.setFirstName(firstName);
    }

    @Override
    public String getLastName() {
        return acmeUser.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        acmeUser.setLastName(lastName);
    }

    @Override
    public Long getCreatedTimestamp() {
        return acmeUser.getCreatedTimestamp();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        //NOOP
    }

    @Override
    public boolean isEnabled() {
        return acmeUser.isEnabled();
    }

    @Override
    public boolean isEmailVerified() {

        // email verified is always true for acme users
        return true;
    }

    @Override
    public void setEmailVerified(boolean verified) {
        // NOOP
    }

    @Override
    public void setEnabled(boolean enabled) {
        // super.setEnabled(enabled);
    }

    //    @Override
//    public Map<String, List<String>> getAttributes() {
//        return super.getAttributes();
//    }


    @Override
    public Set<RoleModel> getRoleMappings() {
        Set<RoleModel> roleMappings = super.getRoleMappings();


        FlyweightAcmeUserStorageProvider externalUserStorageProvider = getExternalUserStorageProvider();
        Set<RoleModel> externalRoleMappings = externalUserStorageProvider.getRoleMappings(realm, getId());

        roleMappings.addAll(externalRoleMappings);

        return roleMappings;
    }

    @Override
    public Set<RoleModel> getClientRoleMappings(ClientModel app) {
        return super.getClientRoleMappings(app);
    }
}
