package com.github.thomasdarimont.keycloak.federation.client;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.TokenVerifier;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;
import org.keycloak.util.JWKSUtils;

import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JBossLog
public class RemoteTokenVerifier {

    // TODO replace with a time bound cache
    private static final ConcurrentHashMap<String, PublicKey> JWKS_CACHE = new ConcurrentHashMap<>();

    private final RemoteKeycloakClient keycloakFacade;

    private final String realm;

    private final String realmUrl;

    public RemoteTokenVerifier(RemoteKeycloakClient keycloakFacade, String authServerUrl, String realm) {
        this.keycloakFacade = keycloakFacade;
        this.realm = realm;
        this.realmUrl = authServerUrl + "/realms/" + realm;
    }

    public AccessToken verifyAccessToken(AccessTokenResponse accessTokenResponse) {
        return verifyToken(accessTokenResponse, AccessToken.class);
    }

    public IDToken verifyIDToken(AccessTokenResponse accessTokenResponse) {
        return verifyToken(accessTokenResponse, IDToken.class);
    }

    private <T extends IDToken> T verifyToken(AccessTokenResponse accessTokenResponse, Class<T> tokenClass) {

        String tokenString = accessTokenResponse.getToken();
        TokenVerifier<T> verifier = TokenVerifier.create(tokenString, tokenClass) //
                .withDefaultChecks() //
                .realmUrl(realmUrl)
                // .withChecks()
                ;

        T token;
        try {
            String keyId = verifier.getHeader().getKeyId();
            PublicKey publicKey = JWKS_CACHE.computeIfAbsent(keyId, kid -> {
                JSONWebKeySet jwks = keycloakFacade.getJwks(realm);
                Map<String, PublicKey> publicKeys = JWKSUtils.getKeysForUse(jwks, JWK.Use.SIG);
                return publicKeys.get(kid);
            });
            verifier.publicKey(publicKey);
            verifier.verify();
            token = verifier.getToken();
        } catch (Exception ex) {
            log.warnf(ex, "Failed to verify token");
            return null;
        }
        return token;
    }
}
