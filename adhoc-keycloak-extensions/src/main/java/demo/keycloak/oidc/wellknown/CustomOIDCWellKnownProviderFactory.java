package demo.keycloak.oidc.wellknown;

import com.google.auto.service.AutoService;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCWellKnownProvider;
import org.keycloak.protocol.oidc.OIDCWellKnownProviderFactory;
import org.keycloak.wellknown.WellKnownProvider;
import org.keycloak.wellknown.WellKnownProviderFactory;

@AutoService(WellKnownProviderFactory.class)
public class CustomOIDCWellKnownProviderFactory extends OIDCWellKnownProviderFactory {

    @Override
    public WellKnownProvider create(KeycloakSession session) {
        return new CustomOIDCWellKnownProvider(session, (OIDCWellKnownProvider) super.create(session));
    }
}
