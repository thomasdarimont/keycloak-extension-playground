package demo.keycloak.oidcmappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.PairwiseSubMapperHelper;
import org.keycloak.protocol.oidc.mappers.SHA256PairwiseSubMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserPropertyMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.IDToken;
import org.keycloak.util.JsonSerialization;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JBossLog
@AutoService(ProtocolMapper.class)
public class PairwiseSubCollectorOidcMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String PROVIDER_ID = "oidc-pairwise-collector-protocol-mapper";

    private static final String PAIRWISE_SUB_MAPPER_NAME = "pairwiseSubMapperName";

    private static final String ORIGINAL_USER_SUB_KEY = "originalUserSubKey";

    private static final String DEFAULT_ORIGINAL_USER_SUB_KEY = "keycloak";

    private static final String DEFAULT_PAIRWISE_SUB_MAPPER_NAME = "pairwise-sub";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property()
                .name(PAIRWISE_SUB_MAPPER_NAME)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Pairwise Sub Mapper Name")
                .helpText("Name of pairwise-sub mapper to look for in a client configuration. Defaults to 'pairwise-sub'.")
                .defaultValue(DEFAULT_PAIRWISE_SUB_MAPPER_NAME)
                .add()
                .property()
                .name(ORIGINAL_USER_SUB_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Original user sub Key")
                .helpText("Key to use for the original user sub. Defaults to 'keycloak'.")
                .defaultValue(DEFAULT_ORIGINAL_USER_SUB_KEY)
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
        return "Demo Collect Pairwise Sub Mapper";
    }

    @Override
    public String getHelpText() {
        return "Collects the pairwise subject identifiers for the given user and the given clients.";
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

        HttpRequest httpRequest = keycloakSession.getContext().getContextObject(HttpRequest.class);
        MultivaluedMap<String, String> formParams = httpRequest.getDecodedFormParameters();
        String targetUserId = formParams.getFirst("targetUserId");
        String clients = formParams.getFirst("targetClients");

        if (targetUserId == null || clients == null) {
            return;
        }

        Map<String, String> config = mappingModel.getConfig();

        String originalUserSubKey = DEFAULT_ORIGINAL_USER_SUB_KEY;
        String pairwiseSubMapperName = DEFAULT_PAIRWISE_SUB_MAPPER_NAME;
        if (config != null) {
            originalUserSubKey = config.getOrDefault(ORIGINAL_USER_SUB_KEY, originalUserSubKey);
            pairwiseSubMapperName = config.getOrDefault(PAIRWISE_SUB_MAPPER_NAME, pairwiseSubMapperName);
        }

        Map<String, Object> data = new HashMap<>();

        SHA256PairwiseSubMapper subMapper = new SHA256PairwiseSubMapper();

        data.put(originalUserSubKey, targetUserId);

        for (String clientId : clients.split(" ")) {
            ClientModel client = keycloakSession.getContext().getRealm().getClientByClientId(clientId);
            if (client == null) {
                continue;
            }
            ProtocolMapperModel mapperModel = client.getProtocolMapperByName("openid-connect", pairwiseSubMapperName);
            if (mapperModel == null) {
                continue;
            }
            String clientSub = subMapper.generateSub(mapperModel, mapperModel.getConfig().get(PairwiseSubMapperHelper.SECTOR_IDENTIFIER_URI), targetUserId);
            data.put(clientId, clientSub);
        }


        JsonNode claimValue;
        try {
            claimValue = JsonSerialization.createObjectNode(data);
        } catch (IOException ioe) {
            log.warnf("Could not convert object to jsonNode.", ioe);
            return;
        }

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
    }

}