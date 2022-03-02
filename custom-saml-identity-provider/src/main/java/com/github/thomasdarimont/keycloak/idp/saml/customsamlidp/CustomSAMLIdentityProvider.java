package com.github.thomasdarimont.keycloak.idp.saml.customsamlidp;

import org.keycloak.broker.saml.SAMLIdentityProvider;
import org.keycloak.broker.saml.SAMLIdentityProviderConfig;
import org.keycloak.models.KeycloakSession;
import org.keycloak.saml.validators.DestinationValidator;

public class CustomSAMLIdentityProvider extends SAMLIdentityProvider {

    public CustomSAMLIdentityProvider(KeycloakSession session, SAMLIdentityProviderConfig config, DestinationValidator destinationValidator) {
        super(session, config, destinationValidator);
    }
}
