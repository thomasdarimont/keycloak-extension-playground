package demo.keycloak.oidcmappers;

import com.google.auto.service.AutoService;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserPropertyMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.IDToken;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.LDAPUtils;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.EscapeStrategy;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore;

import java.util.List;
import java.util.Optional;

@AutoService(ProtocolMapper.class)
public class LdapQueryOidcMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String PROVIDER_ID = "oidc-ldapquery-protocol-mapper";

    private static final Logger LOGGER = Logger.getLogger(LdapQueryOidcMapper.class);

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    private static final String SOME_PROPERTY = "someProperty";

    static {

        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property()
                .name(SOME_PROPERTY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Some Property")
                .helpText("Some property help")
                //.defaultValue("http://localhost:")
                .add()
                .build();

        OIDCAttributeMapperHelper.addAttributeConfig(CONFIG_PROPERTIES, UserPropertyMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Demo LDAP Query Mapper";
    }

    @Override
    public String getHelpText() {
        return "A protocol mapper that can fetch additional claims via ldap query";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {

        Object claimValue = fetchLdapClaims(mappingModel, userSession, keycloakSession);

        LOGGER.infof("setClaim %s=%s", mappingModel.getName(), claimValue);

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
    }

    private Object fetchLdapClaims(ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession) {

//        RealmModel realm = keycloakSession.getContext().getRealm();
//
//        Optional<ComponentModel> ldapConfigHolder = realm.getComponents().stream().filter(c -> "ldap".equals(c.getProviderId()) && "ldap-test".equals(c.getName())).findAny();
//
//        if (!ldapConfigHolder.isPresent()) {
//            return null;
//        }
//
//        ComponentModel ldapConfig = ldapConfigHolder.get();
//
//        LDAPStorageProvider ldapStorageProvider = keycloakSession.getProvider(LDAPStorageProvider.class, ldapConfig);
//        LDAPIdentityStore ldapIdentityStore = ldapStorageProvider.getLdapIdentityStore();

//        try (LDAPQuery ldapQuery = LDAPUtils.createQueryForUserSearch(ldapStorageProvider, realm)) {
//            LDAPQueryConditionsBuilder conditionsBuilder = new LDAPQueryConditionsBuilder();
//
//            // Mapper should replace "email" in parameter name with correct LDAP mapped attribute
//            Condition emailCondition = conditionsBuilder.equal(UserModel.EMAIL, "test@localhost", EscapeStrategy.DEFAULT);
//            ldapQuery.addWhereCondition(emailCondition);
//
//            List<LDAPObject> ldapQueryResults = ldapIdentityStore.fetchQueryResults(ldapQuery);
//
//            // do something with ldapQueryResults
//        }

//        LDAPQuery ldapQuery = new LDAPQuery(ldapStorageProvider);
//        LDAPConfig config = ldapIdentityStore.getConfig();
//        ldapQuery.setSearchScope(config.getSearchScope());
//        ldapQuery.setSearchDn(config.getUsersDn());
//        ldapQuery.addObjectClasses(config.getUserObjectClasses());
//
//        String customFilter = config.getCustomUserSearchFilter();
//        if (customFilter != null) {
//            Condition customFilterCondition = new LDAPQueryConditionsBuilder().addCustomLDAPFilter(customFilter);
//            ldapQuery.addWhereCondition(customFilterCondition);
//        }

        return "value-from-ldap";
    }
}