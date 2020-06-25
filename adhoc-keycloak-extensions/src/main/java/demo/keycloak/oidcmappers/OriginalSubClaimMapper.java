package demo.keycloak.oidcmappers;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
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

import java.util.List;
import java.util.Set;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OriginalSubClaimMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String PROVIDER_ID = "oidc-original-idp-sub-protocol-mapper";

    private static final String IDENTITY_PROVIDER_ALIAS = "idp-alias";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property()
                .name(IDENTITY_PROVIDER_ALIAS)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("IdP-Alias")
                .helpText("Name of Identity Provider Alias to lookup the sub.")
                .defaultValue("kc-oidc-acme-tenant1-users")
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
        return "Demo linked Identity Provider Id Mapper";
    }

    @Override
    public String getHelpText() {
        return "Exposes the id of the referenced Identity Brokered user as dedicated claim.";
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
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession session, ClientSessionContext clientSessionCtx) {

        RealmModel realm = userSession.getRealm();
        UserModel user = userSession.getUser();

        List<IdentityProviderModel> identityProviders = realm.getIdentityProviders();
        Set<FederatedIdentityModel> identities = session.users().getFederatedIdentities(user, realm);

        if (identityProviders == null || identityProviders.isEmpty()) {
            return;
        }

        for (IdentityProviderModel provider : identityProviders) {
            if (!provider.isEnabled()) {
                continue;
            }

            String providerId = provider.getAlias();
            FederatedIdentityModel identity = getIdentity(identities, providerId);

            if (identity != null) {
                String userId = identity.getUserId();
                OIDCAttributeMapperHelper.mapClaim(token, mappingModel, userId);
            }
        }
    }

    private FederatedIdentityModel getIdentity(Set<FederatedIdentityModel> identities, String providerId) {

        for (FederatedIdentityModel link : identities) {
            if (providerId.equals(link.getIdentityProvider())) {
                return link;
            }
        }

        return null;
    }
}