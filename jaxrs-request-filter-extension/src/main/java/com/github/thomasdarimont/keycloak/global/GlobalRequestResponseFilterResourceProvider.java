package com.github.thomasdarimont.keycloak.global;

import com.google.auto.service.AutoService;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@AutoService(RealmResourceProviderFactory.class)
public class GlobalRequestResponseFilterResourceProvider implements RealmResourceProviderFactory {

    private static final DummyResourceProvider INSTANCE = new DummyResourceProvider();

    private static final String PROVIDER_ID = "global-filter-extension";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public void init(Config.Scope config) {
        ResteasyProviderFactory.getInstance().getContainerRequestFilterRegistry()
                .registerSingleton(GlobalRequestResponseFilter.INSTANCE);

        ResteasyProviderFactory.getInstance().getContainerResponseFilterRegistry()
                .registerSingleton(GlobalRequestResponseFilter.INSTANCE);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    static class DummyResourceProvider implements RealmResourceProvider {

        @Override
        public Object getResource() {
            // dummy resource
            return new Object();
        }

        @Override
        public void close() {
            // NOOP
        }
    }
}
