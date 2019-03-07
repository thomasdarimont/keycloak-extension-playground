package com.github.thomasdarimont.keycloak.saml.simple;

import org.jboss.logging.Logger;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
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

        // transform attributeStatement here
        LOGGER.infof("transformAttributeStatement");

        AttributeType bubu = new AttributeType("bubu");
        bubu.setFriendlyName("FriendlyBubu");
        bubu.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
        bubu.setName("Bubu");

        bubu.addAttributeValue("Object allowed but only Strings or NameIDType supported here...");
        // see: bottom of org.keycloak.saml.processing.core.saml.v2.writers.BaseWriter.writeAttributeTypeWithoutRootTag

        attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(bubu));
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
