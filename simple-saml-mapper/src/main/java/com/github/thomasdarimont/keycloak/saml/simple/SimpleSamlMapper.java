package com.github.thomasdarimont.keycloak.saml.simple;

import org.jboss.logging.Logger;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.saml.mappers.AbstractSAMLProtocolMapper;
import org.keycloak.protocol.saml.mappers.AttributeStatementHelper;
import org.keycloak.protocol.saml.mappers.SAMLAttributeStatementMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

public class SimpleSamlMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper {

    private static final String PROVIDER_ID = "saml-simple-protocol-mapper";

    private static final Logger LOGGER = Logger.getLogger(SimpleSamlMapper.class);

    private static final String CONFIG_PROPERTY = "configProperty";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property()
                .name(CONFIG_PROPERTY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Config value")
                .helpText("A description for the config value")
                .defaultValue("defaultValue")
                .add()
// additional properties here
                .build();

    }

    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Demo Simple SAML Mapper";
    }

    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {


        Object attributeValue;
        try {

            attributeValue = mappingModel.getConfig().getOrDefault(CONFIG_PROPERTY, "defaultProperty");
            LOGGER.infof("setClaim %s=%s", mappingModel.getName(), attributeValue);

            // single value case
            AttributeStatementHelper.addAttribute(attributeStatement, mappingModel, attributeValue.toString());

        } catch (Exception ex) {
            LOGGER.error("Error during execution of ProtocolMapper script", ex);
            AttributeStatementHelper.addAttribute(attributeStatement, mappingModel, null);
        }
    }

    @Override
    public String getHelpText() {
        return "Simple SAML Attribute Mapper help";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
