package com.github.thomasdarimont.keycloak.virtualclients.mappers;

import com.google.auto.service.AutoService;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoService(ProtocolMapper.class)
public class DynamicClaimMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "oidc-dynamic-protocol-mapper";

    private static final Logger LOGGER = Logger.getLogger(DynamicClaimMapper.class);

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Demo Dynamic Mapper";
    }

    @Override
    public String getHelpText() {
        return "A simple dynamic token mapper";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {

        // compute client data from remote location

        LOGGER.infof("Compute dynamic claims for virtual client %s", token.getIssuedFor());

        Map<String, Object> data = new HashMap<>();
        data.put("time", Instant.now().toString());
        data.put("clientInfo", token.issuedFor);

        token.getOtherClaims().put("data", data);
    }
}

