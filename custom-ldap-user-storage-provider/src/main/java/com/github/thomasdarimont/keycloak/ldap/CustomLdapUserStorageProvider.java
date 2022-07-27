package com.github.thomasdarimont.keycloak.ldap;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.ldap.LDAPIdentityStoreRegistry;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.LDAPStorageProviderFactory;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore;

public class CustomLdapUserStorageProvider extends LDAPStorageProvider {

    public CustomLdapUserStorageProvider(LDAPStorageProviderFactory factory, KeycloakSession session, ComponentModel model, LDAPIdentityStore ldapIdentityStore) {
        super(factory, session, model, ldapIdentityStore);
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {

        // disable user instead of removing it
        user.setEnabled(false);

//        return super.removeUser(realm, user);

        return true;
    }

    @AutoService(UserStorageProviderFactory.class)
    public static class Factory extends LDAPStorageProviderFactory {

        @Override
        public String getId() {

            // return super.getId(); // use this to replace the default "ldap" LDAPStorageProviderFactory
            return "custom-ldap";
        }

        @Override
        public String getHelpText() {
            return "Custom LDAP User Store which disables users on remove.";
        }

        // explicitly shadows parent ldapStoreRegistry
        private LDAPIdentityStoreRegistry ldapStoreRegistry;

        @Override
        public LDAPStorageProvider create(KeycloakSession session, ComponentModel model) {
            var configDecorators = getLDAPConfigDecorators(session, model);
            var ldapIdentityStore = ldapStoreRegistry.getLdapStore(session, model, configDecorators);
            return new CustomLdapUserStorageProvider(this, session, model, ldapIdentityStore);
        }

        public void init(Config.Scope config) {
            this.ldapStoreRegistry = new LDAPIdentityStoreRegistry();
        }

        @Override
        public void close() {
            this.ldapStoreRegistry = null;
        }
    }
}
