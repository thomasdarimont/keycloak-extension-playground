package demo.keycloak.userstorage.ldap.attributeimport;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;

public class CustomLdapAttributeImportMapper extends AbstractLDAPStorageMapper {

    static final String ID = "demo-ldapimport-mapper";

    static final String ATTRIBUTES_TO_IMPORT_KEY = "attributesToImport";
    private final ComponentModel componentModel;

    public CustomLdapAttributeImportMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
        super(mapperModel, ldapProvider);
        this.componentModel = mapperModel;
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        // NOOP
    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {
        // NOOP
    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return delegate;
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {

        String attributeCsv = componentModel.getConfig().getFirst(ATTRIBUTES_TO_IMPORT_KEY);
        if (attributeCsv == null || attributeCsv.trim().isBlank()) {
            return;
        }

        for (String attributeCandidate : attributeCsv.trim().split(",")) {
            String attribute = attributeCandidate.trim();
            if (attribute.isBlank()) {
                continue;
            }
            query.addReturningLdapAttribute(attributeCandidate);
        }


    }
}
