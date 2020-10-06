package demo;

import org.keycloak.TokenVerifier;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.forms.login.freemarker.model.UrlBean;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;
import org.keycloak.util.JsonSerialization;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TokenResource {

    private KeycloakSession session;
    private final Theme theme;
    private final FreeMarkerUtil freemarker;

    public TokenResource(KeycloakSession session) {
        try {
            this.session = session;
            theme = session.theme().getTheme(Theme.Type.LOGIN);
            freemarker = new FreeMarkerUtil();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    public Response getForm() throws IOException, FreeMarkerException {

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("properties", theme.getProperties());
        attributes.put("url", new UrlBean(session.getContext().getRealm(), theme, session.getContext().getAuthServerUrl(), null));

        String response = freemarker.processTemplate(attributes, "token-validator.ftl", theme);
        return Response.ok(response).build();
    }

    @POST
    public Response parseToken(@FormParam("token") String tokenStringInput) throws IOException, FreeMarkerException {

        String tokenString = tokenStringInput.trim();

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("properties", theme.getProperties());

        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();

        attributes.put("url", new UrlBean(realm, theme, context.getAuthServerUrl(), null));

        try {
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(tokenString, AccessToken.class).withChecks(
                    TokenVerifier.IS_ACTIVE,
                    new TokenVerifier.RealmUrlCheck(Urls.realmIssuer(context.getAuthServerUrl(), realm.getName()))
            );

            attributes.put("tokenLength", tokenString.getBytes(StandardCharsets.UTF_8).length);
            attributes.put("token", tokenString);
            attributes.put("header", JsonSerialization.writeValueAsPrettyString(verifier.getHeader()));
            AccessToken token = verifier.getToken();
            attributes.put("tokenParsed", JsonSerialization.writeValueAsPrettyString(token));

            String kid = verifier.getHeader().getKeyId();
            String algorithm = verifier.getHeader().getAlgorithm().name();

            verifier.verifierContext(session.getProvider(SignatureProvider.class, algorithm).verifier(kid));

            String activeKid = session.keys().getActiveKey(realm, KeyUse.SIG, algorithm).getKid();

            verifier.verify();

            UserSessionModel userSession = session.sessions().getUserSession(realm, token.getSessionState());
            if (!AuthenticationManager.isSessionValid(realm, userSession)) {
                throw new Exception("Session not active");
            }

            UserModel user = userSession.getUser();
            if (user == null) {
                throw new Exception("Unknown user");
            }

            if (!user.isEnabled()) {
                throw new Exception("User disabled");
            }

            ClientModel client = realm.getClientByClientId(token.getIssuedFor());

            if (token.getIat() < client.getNotBefore()) {
                throw new Exception("Stale token");
            }
            if (token.getIat() < realm.getNotBefore()) {
                throw new Exception("Stale token");
            }
            if (token.getIat() < session.users().getNotBeforeOfUser(realm, user)) {
                throw new Exception("Stale token");
            }

            attributes.put("valid", Boolean.TRUE);
            attributes.put("activeKey", activeKid.equals(kid));

        } catch (Exception e) {
            attributes.put("valid", Boolean.FALSE);
            attributes.put("error", e.getMessage());
        }

        return Response.ok(freemarker.processTemplate(attributes, "token-validator.ftl", theme)).build();
    }

}