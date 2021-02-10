package demo.keycloak.userstorage.ldap.attributeimport;

import com.google.auto.service.AutoService;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapperFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AutoService(LDAPStorageMapperFactory.class)
public class CustomLdapAttributeImportMapperFactory extends AbstractLDAPStorageMapperFactory {

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {

        ProviderConfigProperty attributesToImport = createConfigProperty(
                CustomLdapAttributeImportMapper.ATTRIBUTES_TO_IMPORT_KEY,
                "Attributes to Import",
                "Addtional LDAP Attributes to import as comma separeted list, like e.g. entryUUID",
                ProviderConfigProperty.STRING_TYPE,
                Collections.emptyList());

        CONFIG_PROPERTIES = Arrays.asList(attributesToImport);
    }

    @Override
    protected AbstractLDAPStorageMapper createMapper(ComponentModel mapperModel, LDAPStorageProvider federationProvider) {
        return new CustomLdapAttributeImportMapper(mapperModel, federationProvider);
    }

    @Override
    public String getId() {
        return CustomLdapAttributeImportMapper.ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties(RealmModel realm, ComponentModel parent) {
        return CONFIG_PROPERTIES;
    }
}
