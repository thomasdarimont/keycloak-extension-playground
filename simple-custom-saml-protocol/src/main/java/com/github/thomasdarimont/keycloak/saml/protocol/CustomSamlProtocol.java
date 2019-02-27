package com.github.thomasdarimont.keycloak.saml.protocol;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.protocol.saml.JaxrsSAML2BindingBuilder;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.ws.rs.core.Response;
import java.io.IOException;

public class CustomSamlProtocol extends SamlProtocol {

    protected Response buildAuthenticatedResponse(AuthenticatedClientSessionModel clientSession, String redirectUri, Document samlDocument, JaxrsSAML2BindingBuilder bindingBuilder) throws ConfigurationException, ProcessingException, IOException {

        // TODO use Keycloak provider information from this.session

        Element attributeStatementElement = (Element) samlDocument.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), "AttributeStatement").item(0);
        // TODO pull information from user attributes

        attributeStatementElement.appendChild(newSamlAttributeElement(samlDocument, null, "Role", JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get(), "dummy", "xsd:string"));
        attributeStatementElement.appendChild(newSamlAttributeElement(samlDocument, "XSPA Organization ID", "urn:oasis:names:tc:xspa:1.0:subject:organization-id", JBossSAMLURIConstants.ATTRIBUTE_FORMAT_URI.get(), "urn:oid:1.2.3.4.5.6.7.8.9.10.11.12", "xsd:anyURI"));

        Element roleElement = samlDocument.createElementNS("urn:hl7-org:v3", "Role");
        roleElement.setAttribute("code", "PRA");
        roleElement.setAttribute("codeSystem", "1.2.3.4.5.6.7.8.9.10.11.12");
        roleElement.setAttribute("codeSystemName", "IHEXDShealthcareFacilityTypeCode");
        roleElement.setAttribute("displayName", "Arztpraxis");
        attributeStatementElement.appendChild(newSamlAttributeElement(samlDocument, "Acme Role", "urn:oasis:names:tc:xacml:2.0:subject:role", JBossSAMLURIConstants.ATTRIBUTE_FORMAT_URI.get(), roleElement, "xsd:anyType"));

        return super.buildAuthenticatedResponse(clientSession, redirectUri, samlDocument, bindingBuilder);
    }

    private Element newSamlAttributeElement(Document samlDocument, String friendlyName, String name, String nameFormat, Object value, String type) {

        Element targetSamlAttributeElement = samlDocument.createElementNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), "Attribute");

        if (friendlyName != null) {
            targetSamlAttributeElement.setAttribute("FriendlyName", friendlyName);
        }
        targetSamlAttributeElement.setAttribute("Name", name);
        if (nameFormat != null) {
            targetSamlAttributeElement.setAttribute("NameFormat", nameFormat);
        }

        Element samlAttributeValue = samlDocument.createElementNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), "AttributeValue");
        samlAttributeValue.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        samlAttributeValue.setAttribute("xsi:type", type);
        targetSamlAttributeElement.appendChild(samlAttributeValue);

        if (value instanceof String) {
            samlAttributeValue.setTextContent((String) value);
        } else if (value instanceof Element) {
            samlAttributeValue.appendChild((Element) value);
        } else if (value != null) {
            samlAttributeValue.setTextContent(value.toString());
        } else {
            samlAttributeValue.setTextContent(String.valueOf(value));
        }

        return targetSamlAttributeElement;
    }
}
