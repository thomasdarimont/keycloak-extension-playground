package com.github.thomasdarimont.keycloak.saml.protocol;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.saml.SamlProtocolFactory;

public class CustomSamlProtocolFactory extends SamlProtocolFactory {

    public LoginProtocol create(KeycloakSession session) {
        return (new CustomSamlProtocol()).setSession(session);
    }
}
