package demo;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * http://localhost:8081/auth/realms/master/resources/jwt
 */
@AutoService(RealmResourceProviderFactory.class)
public class TokenValidatorFactory implements RealmResourceProviderFactory, RealmResourceProvider {

    private KeycloakSession session;

    public RealmResourceProvider create(KeycloakSession session) {
        this.session = session;
        return this;
    }

    public void init(Config.Scope config) {
        // NOOP
    }

    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    public void close() {
        // NOOP
    }

    public String getId() {
        return "jwt";
    }

    public Object getResource() {
        return new TokenResource(session);
    }

}