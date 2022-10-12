package demo.keycloak.endpoints;

import org.keycloak.authorization.util.Tokens;
import org.keycloak.common.util.Resteasy;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.cache.UserCache;
import org.keycloak.representations.AccessToken;

import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


public class CustomRealmResource {

    /**
     * {@code
     * curl -v http://localhost:8081/auth/realms/demo/custom-resources/ping | jq -C .
     * }
     */
    @GET
    @Path("ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {

        var session = getSession();
        var token = Tokens.getAccessToken(session);

        Map<String, Object> payload = new HashMap<>();
        payload.put("realm", session.getContext().getRealm().getName());
        payload.put("user", token == null ? "anonymous" : token.getPreferredUsername());
        payload.put("timestamp", System.currentTimeMillis());

        return Response.ok(payload).build();
    }

    /**
     * {@code
     * curl -v -H "Authorization: Bearer $KC_ACCESS_TOKEN" -d "userId=1bffdf3a-9453-4be4-bdec-3c743db5fd6a" http://localhost:8081/auth/realms/demo/custom-resources/evict-user
     * }
     */
    @POST
    @Path("evict-user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response evictUserFromCache(@FormParam("userId") String userId) {

        var session = getSession();
        var context = session.getContext();
        var token = Tokens.getAccessToken(session);

        checkRealmAdmin(token, context);

        var realm = context.getRealm();
        var user = session.users().getUserById(realm, userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        UserCache userCache = session.getProvider(UserCache.class);
        if (userCache != null) {
            userCache.evict(realm, user);
        }

        return Response.noContent().build();
    }

    private void checkRealmAdmin(AccessToken token, KeycloakContext context) {

        if (token == null) {
            throw new NotAuthorizedException("Bearer");
        }

        AccessToken.Access realmManagementAccess = token.getResourceAccess(Constants.REALM_MANAGEMENT_CLIENT_ID);
        if (realmManagementAccess != null && realmManagementAccess.isUserInRole("realm-admin")) {
            return;
        }

        AccessToken.Access realmAccess = token.getRealmAccess();
        if (realmAccess == null || !realmAccess.isUserInRole("admin")) {
            throw new ForbiddenException("Does not have realm admin role");
        }
    }

    private KeycloakSession getSession() {
        return Resteasy.getContextData(KeycloakSession.class);
    }
}
