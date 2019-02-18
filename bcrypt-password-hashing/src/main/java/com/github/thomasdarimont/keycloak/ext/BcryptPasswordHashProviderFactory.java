package com.github.thomasdarimont.keycloak.ext;

import org.keycloak.Config;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.credential.hash.PasswordHashProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.Collections;
import java.util.List;

public class BcryptPasswordHashProviderFactory implements PasswordHashProviderFactory, ConfiguredProvider {

    public static final String ID = "bcrypt";

    public static final int DEFAULT_ITERATIONS = 15;

    private int iterations;

    @Override
    public PasswordHashProvider create(KeycloakSession session) {
        return new BcryptPasswordHashProvider(ID, iterations);
    }

    @Override
    public void init(Config.Scope config) {
        this.iterations = config.getInt("iterations", DEFAULT_ITERATIONS);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void close() {
    }

    @Override
    public String getHelpText() {
        return "Provides support for bcrypt password hashes";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        List<ProviderConfigProperty> config = ProviderConfigurationBuilder.create()
                .property().name("iterations").type(ProviderConfigProperty.STRING_TYPE).defaultValue(DEFAULT_ITERATIONS).add()
                .build();
        return Collections.unmodifiableList(config);
    }
}
