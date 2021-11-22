package com.github.thomasdarimont.keycloak.federation.client;

import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

public interface KeycloakFacadeProvider {

    KeycloakFacade getKeycloakFacade();

    AccessToken verifyAccessToken(AccessTokenResponse accessTokenResponse);

    IDToken verifyIDToken(AccessTokenResponse accessTokenResponse);
}
