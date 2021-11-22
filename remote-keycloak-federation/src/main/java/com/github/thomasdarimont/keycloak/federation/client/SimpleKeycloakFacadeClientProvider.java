package com.github.thomasdarimont.keycloak.federation.client;

import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

@JBossLog
public class SimpleKeycloakFacadeClientProvider implements RemoteKeycloakClientProvider {

    private final AtomicReference<ExpiringAccessToken> accessTokenResponseHolder = new AtomicReference<>();

    private final String clientId;

    private final String clientSecret;

    private final String realm;

    private final String authServerUrl;

    private final RemoteKeycloakClient remoteKeycloakClient;

    private final RemoteTokenVerifier remoteTokenVerifier;

    public SimpleKeycloakFacadeClientProvider(ComponentModel componentModel, Function<ComponentModel, ResteasyClient> clientFactory) {
        this.clientId = componentModel.get("clientId");
        this.clientSecret = componentModel.get("clientSecret");
        this.realm = componentModel.get("realm");
        this.authServerUrl = componentModel.get("authServerUrl", "http://localhost:8080/auth");
        this.remoteKeycloakClient = createRemoteKeycloakClient(clientFactory.apply(componentModel));
        this.remoteTokenVerifier = new RemoteTokenVerifier(remoteKeycloakClient, authServerUrl, realm);
    }

    private RemoteKeycloakClient createRemoteKeycloakClient(ResteasyClient client) {
        ResteasyWebTarget webTarget = client.target(UriBuilder.fromPath(this.authServerUrl));
        webTarget.register(new AccessTokenInterceptor(this::getAccessToken));
        return webTarget.proxy(RemoteKeycloakClient.class);
    }

    @Override
    public RemoteKeycloakClient getRemoteKeycloakClient() {
        return remoteKeycloakClient;
    }

    private String getAccessToken() {

        ExpiringAccessToken current = accessTokenResponseHolder.get();
        if (current != null && current.getAccessToken() != null && Time.currentTime() < current.getExpireAt()) {
            return current.getAccessTokenString();
        }

        AccessTokenResponse newAccessTokenResponse = remoteKeycloakClient.getToken( //
                realm, //
                clientId, //
                clientSecret, //
                "roles", // additional scope
                OAuth2Constants.CLIENT_CREDENTIALS);

        AccessToken accessToken = verifyAccessToken(newAccessTokenResponse);
        if (accessToken == null) {
            return null;
        }

        long expireAt = Time.currentTime() + newAccessTokenResponse.getExpiresIn() - 3;
        accessTokenResponseHolder.set(new ExpiringAccessToken(expireAt, accessToken, newAccessTokenResponse));
        return newAccessTokenResponse.getToken();
    }

    @Override
    public AccessToken verifyAccessToken(AccessTokenResponse accessTokenResponse) {
        return remoteTokenVerifier.verifyAccessToken(accessTokenResponse);
    }

    @Override
    public IDToken verifyIDToken(AccessTokenResponse accessTokenResponse) {
        return remoteTokenVerifier.verifyIDToken(accessTokenResponse);
    }

    private static class ExpiringAccessToken {

        private final long expireAt;

        private final AccessToken accessToken;

        private final AccessTokenResponse accessTokenResponse;

        public ExpiringAccessToken(long expireAt, AccessToken accessToken, AccessTokenResponse accessTokenResponse) {
            this.expireAt = expireAt;
            this.accessToken = accessToken;
            this.accessTokenResponse = accessTokenResponse;
        }

        public String getAccessTokenString() {
            if (accessTokenResponse == null) {
                return null;
            }

            return accessTokenResponse.getToken();
        }

        public AccessToken getAccessToken() {
            return accessToken;
        }

        public long getExpireAt() {
            return expireAt;
        }
    }

    private static class AccessTokenInterceptor implements ClientRequestFilter {

        private final Supplier<String> accessTokenSupplier;

        public AccessTokenInterceptor(Supplier<String> accessTokenSupplier) {
            this.accessTokenSupplier = accessTokenSupplier;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            String requestPath = requestContext.getUri().getPath();

            // token request
            if (requestPath.endsWith("/token")) {
                return;
            }

            // JWKS request
            if (requestPath.endsWith("/certs")) {
                return;
            }

            String accessToken = accessTokenSupplier.get();
            if (accessToken == null) {
                return;
            }

            requestContext.getHeaders().addFirst(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
    }
}
