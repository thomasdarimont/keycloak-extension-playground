package demo.keycloak.oidcmappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.service.AutoService;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.util.SimpleHttp;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@AutoService(ProtocolMapper.class)
public class RemoteOidcMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String PROVIDER_ID = "oidc-remote-protocol-mapper";

    private static final Logger LOGGER = Logger.getLogger(RemoteOidcMapper.class);

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    private static final String REMOTE_URL_PROPERTY = "remoteUrl";

    static {

        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property()
                .name(REMOTE_URL_PROPERTY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Remote URL")
                .helpText("URL to fetch custom claims for the given user")
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
        return "Demo Remote Mapper";
    }

    @Override
    public String getHelpText() {
        return "A protocol mapper that can fetch additional claims from an external service";
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

        Object claimValue = fetchRemoteClaims(mappingModel, userSession, keycloakSession);

        LOGGER.infof("setClaim %s=%s", mappingModel.getName(), claimValue);

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
    }

    private Object fetchRemoteClaims(ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession) {

        try {
            String remoteUrl = mappingModel.getConfig().getOrDefault(REMOTE_URL_PROPERTY, "http://localhost:7777/claims");
            UserModel user = userSession.getUser();
            String url = remoteUrl + "?userId=" + user.getId() + "&username=" + URLEncoder.encode(user.getUsername(), "UTF-8");
            JsonNode jsonNode = SimpleHttp.doGet(url, keycloakSession).asJson();
            return jsonNode;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}