package com.github.thomasdarimont.keycloak.simple;

import lombok.RequiredArgsConstructor;
import org.keycloak.authorization.util.Tokens;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code
 * curl -v http://localhost:8081/auth/realms/demo/simple-resource/ping | jq -C .
 * }
 */
@RequiredArgsConstructor
public class SimpleRealmResource {

    private final KeycloakSession session;
    private final AccessToken token;

    public SimpleRealmResource(KeycloakSession session) {
        this.session = session;
        this.token = Tokens.getAccessToken(session);
    }

    @GET
    @Path("ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {

        Map<String, Object> payload = new HashMap<>();
        payload.put("realm", session.getContext().getRealm().getName());
        payload.put("user", token == null ? "anonymous" : token.getPreferredUsername());
        payload.put("timestamp", System.currentTimeMillis());

        return Response.ok(payload).build();
    }

    private void checkRealmAdmin() {

        if (token == null) {
            throw new NotAuthorizedException("Bearer");
        }

        if (token.getRealmAccess() == null || !token.getRealmAccess().isUserInRole("admin")) {
            throw new ForbiddenException("Does not have realm admin role");
        }
    }
}
