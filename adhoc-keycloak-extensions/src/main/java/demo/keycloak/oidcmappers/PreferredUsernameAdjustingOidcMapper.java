package demo.keycloak.oidcmappers;

import com.google.auto.service.AutoService;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
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

/**
 * <pre>
 * {@code
 * KC_REALM=acme-apps
 * KC_ISSUER=http://localhost:8081/auth/realms/$KC_REALM
 *
 * KC_CLIENT_ID=service-client
 * KC_CLIENT_SECRET=9dc80d0f-366b-444a-8e16-5646f9ee0c9f
 *
 * KC_RESPONSE=$( \
 * curl \
 *   -d "client_id=$KC_CLIENT_ID" \
 *   -d "client_secret=$KC_CLIENT_SECRET" \
 *   -d "grant_type=client_credentials" \
 *   "$KC_ISSUER/protocol/openid-connect/token" \
 * )
 *
 * echo $KC_RESPONSE
 * }
 * </pre>
 */
@AutoService(ProtocolMapper.class)
public class PreferredUsernameAdjustingOidcMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String PROVIDER_ID = "oidc-preferred-username-adjusting-mapper";

    private static final String SOME_PROPERTY_ALIAS = "some-property";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property()
                .name(SOME_PROPERTY_ALIAS)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Some Property")
                .helpText("Some Property Help.")
//                .defaultValue("kc-oidc-acme-tenant1-users")
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
        return "Demo Adjust PreferredUsername";
    }

    @Override
    public String getHelpText() {
        return "Demo Adjust PreferredUsername help.";
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

        UserModel user = userSession.getUser();
        String clientId = token.getIssuedFor();

        // TODO use client to service-account username mapping to massage username in token
        if (user.getUsername().startsWith("service-account-")) {
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, user.getUsername().replaceAll("-", ":"));
        }

    }
}