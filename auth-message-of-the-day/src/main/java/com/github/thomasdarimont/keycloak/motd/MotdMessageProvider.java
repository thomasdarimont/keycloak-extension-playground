package com.github.thomasdarimont.keycloak.motd;

import org.keycloak.models.KeycloakSession;

import java.util.Map;

public interface MotdMessageProvider {

    MotdMessage getMessage(KeycloakSession session, Map<String, String> config);
}
