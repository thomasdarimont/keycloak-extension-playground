package com.github.thomasdarimont.keycloak.idp.saml.customsamlidp;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.saml.SAMLIdentityProvider;
import org.keycloak.broker.saml.SAMLIdentityProviderConfig;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.saml.validators.DestinationValidator;

@JBossLog
@AutoService(IdentityProviderFactory.class)
public class CustomSAMLIdentityProviderFactory extends SAMLIdentityProviderFactory {

    private DestinationValidator destinationValidator;

    @Override
    public SAMLIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new CustomSAMLIdentityProvider(session, new SAMLIdentityProviderConfig(model), destinationValidator);
    }

    @Override
    public void init(Config.Scope config) {
        log.info("Custom SAML Identity Provider initialization");
        super.init(config);
        this.destinationValidator = DestinationValidator.forProtocolMap(config.getArray("knownProtocols"));

    }
}
