package com.github.thomasdarimont.keycloak.oidc.mapper.simple;

import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.IDToken;

import java.util.List;

public class SimpleOidcMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String PROVIDER_ID = "oidc-simple-protocol-mapper";

    private static final Logger LOGGER = Logger.getLogger(SimpleOidcMapper.class);

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    private static final String CONFIG_PROPERTY = "configProperty";

    static {

        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property()
                .name(CONFIG_PROPERTY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Config value")
                .helpText("A description for the config value")
                .defaultValue("defaultValue")
                .add()

                .property()
                .name(ProtocolMapperUtils.MULTIVALUED)
                .label(ProtocolMapperUtils.MULTIVALUED_LABEL)
                .helpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
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
        return "Demo Simple Mapper";
    }

    @Override
    public String getHelpText() {
        return "A simple oidc token mapper";
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

        String claimValue = mappingModel.getConfig().getOrDefault(CONFIG_PROPERTY, "defaultProperty");
        LOGGER.infof("setClaim %s=%s", mappingModel.getName(),claimValue);

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
    }
}
