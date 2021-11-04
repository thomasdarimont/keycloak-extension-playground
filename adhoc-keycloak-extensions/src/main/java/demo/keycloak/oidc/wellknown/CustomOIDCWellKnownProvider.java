package demo.keycloak.oidc.wellknown;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCWellKnownProvider;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.wellknown.WellKnownProvider;

public class CustomOIDCWellKnownProvider implements WellKnownProvider {

    private final KeycloakSession session;
    private final OIDCWellKnownProvider delegate;

    public CustomOIDCWellKnownProvider(KeycloakSession session, OIDCWellKnownProvider delegate) {
        this.session = session;
        this.delegate = delegate;
    }

    @Override
    public Object getConfig() {
        OIDCConfigurationRepresentation config = (OIDCConfigurationRepresentation) delegate.getConfig();

        config.getClaimsSupported().add("customClaim");

        return config;
    }

    @Override
    public void close() {
        // NOOP
    }
}
