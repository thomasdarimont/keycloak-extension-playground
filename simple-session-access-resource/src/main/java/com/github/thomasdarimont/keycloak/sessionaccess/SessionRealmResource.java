package com.github.thomasdarimont.keycloak.sessionaccess;

import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.OAuth2Constants;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.utils.AuthorizeClientUtil;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.Cors;

import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class SessionRealmResource {

    private static final Set<String> PARAMS_TO_IGNORE = Set.of(OAuth2Constants.CLIENT_ID, OAuth2Constants.CLIENT_SECRET);

    private static final String ALLOWED_SESSION_PARAM_PREFIX = "acme_";

    private final KeycloakSession session;
    private final AuthenticationManager.AuthResult auth;


    public SessionRealmResource(KeycloakSession session) {
        this.session = session;
        // Authorization Header is ACCESS_TOKEN
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    /**
     * This propagates given values into the user session referenced by the Access Token used to access this resource.
     * Only confidential clients can perform this operation.
     *
     * This example uses two clients:
     * - acme-frontend (public)
     * - acme-backend (confidential)
     *
     * In order to expose the provided session information in the token, you need to add UserSessionNote mapper to acme-frontend client and map the "acme_tenant" session-note to the "tenant" claim.
     *
     * {@code
     * KC_USERNAME=tester
     * KC_PASSWORD=test
     *
     * KC_CLIENT_ID=acme-frontend
     * KC_ISSUER=http://localhost:8081/auth/realms/propagate-infos-to-session
     *
     * KC_RESPONSE=$( \
     * curl \
     *   -d "client_id=$KC_CLIENT_ID" \
     *   -d "username=$KC_USERNAME" \
     *   -d "password=$KC_PASSWORD" \
     *   -d "grant_type=password" \
     *   "$KC_ISSUER/protocol/openid-connect/token" \
     * )
     * echo $KC_RESPONSE | jq -C .
     *
     * KC_REFRESH_TOKEN=$(echo $KC_RESPONSE | jq -r .refresh_token)
     * KC_ACCESS_TOKEN=$(echo $KC_RESPONSE | jq -r .access_token)
     *
     * echo $KC_ACCESS_TOKEN
     *
     * # Update the session referenced by the access-token in Keycloak. Note that we use user AND client authentication here.
     * curl -v \
     *      -X PATCH \
     *      -H "Authorization: Bearer $KC_ACCESS_TOKEN" \
     *      -d "client_id=acme-backend" \
     *      -d "client_secret=6e0f6eb9-fb06-4997-9c2a-bf47079c4692" \
     *      -d "acme_tenant=42" \
     *      $KC_ISSUER/session-resource/session
     *
     * # Obtain new tokens with refresh-token:
     * KC_RESPONSE=$( \
     *   curl -k \
     *      -H 'Content-Type: application/x-www-form-urlencoded' \
     *      -d "client_id=$KC_CLIENT_ID" \
     *      -d "refresh_token=$KC_REFRESH_TOKEN" \
     *      -d 'grant_type=refresh_token' \
     *     $KC_ISSUER/protocol/openid-connect/token | jq . \
     * )
     *
     * # The new access token should now have the tenant claim
     * KC_ACCESS_TOKEN2=$(echo $KC_RESPONSE | jq -r .access_token)
     * echo $KC_ACCESS_TOKEN2
     * }
     */
    @PATCH
    @Path("session")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response patch(MultivaluedMap<String, String> formParams, @Context HttpRequest request) {

        // checkRealmAdmin();

        RealmModel realm = session.getContext().getRealm();
        EventBuilder event = new EventBuilder(realm, session, session.getContext().getConnection());
        Cors cors = Cors.add(request).auth().allowedMethods("PATCH").auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);

        // Form Body MUST contain client_id, client_secret, client MUST be a confidential client
        AuthorizeClientUtil.authorizeClient(session, event, cors);

        // TODO check if client is allowed to propagate values to the user session

        UserSessionModel session = this.auth.getSession();
        for (String param : formParams.keySet()) {
            // TODO only extract explicitly allowed parameters

            if (PARAMS_TO_IGNORE.contains(param)) {
                continue;
            }

            if (!param.startsWith(ALLOWED_SESSION_PARAM_PREFIX)) {
                continue;
            }

            String value = formParams.getFirst(param);
            session.setNote(param, value);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "session updated");
        return Response.ok(payload).build();
    }

    private void checkRealmAdmin() {

        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        }

        if (auth.getToken().getRealmAccess() == null || !auth.getToken().getRealmAccess().isUserInRole("admin")) {
            throw new ForbiddenException("Does not have realm admin role");
        }
    }
}
