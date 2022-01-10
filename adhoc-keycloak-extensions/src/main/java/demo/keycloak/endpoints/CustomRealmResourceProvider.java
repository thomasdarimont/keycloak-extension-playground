package demo.keycloak.endpoints;

import org.keycloak.services.resource.RealmResourceProvider;

public class CustomRealmResourceProvider implements RealmResourceProvider {

    private static final CustomRealmResource INSTANCE = new CustomRealmResource();

    @Override
    public Object getResource() {
        return INSTANCE;
    }

    @Override
    public void close() {

    }
}
