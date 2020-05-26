package com.github.thomasdarimont.keycloak.auth.sessionprop;

import lombok.Data;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.util.JsonSerialization;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@JBossLog
public class SessionPropagationAuthenticator implements Authenticator {

    static final String SESSION_REFERENCE_MAX_AGE_SECONDS = "sessionReferenceMaxAgeSeconds";

    static final String ENCRYPTION_KEY = "key";

    static final String SESSION_VALIDATION_SERVICE_URL = "sessionValidationServiceUrl";

    private final KeycloakSession session;

    public SessionPropagationAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> queryParameters = context.getHttpRequest().getUri().getQueryParameters();
        String encryptedSessionReferenceData = queryParameters.getFirst("ksr");

        if (encryptedSessionReferenceData == null) {
            log.infof("Reject session propagation. Reason: Missing sessionReferenceData.");
            context.attempted();
            return;
        }

        String encryptedSessionReferenceSalt = queryParameters.getFirst("ksrs");
        if (encryptedSessionReferenceSalt == null) {
            log.infof("Reject session propagation. Reason: Missing encryptedSessionReferenceSalt.");
            context.attempted();
            return;
        }

        log.infof("Attempting user session propagation...");

        // TODO use encryption key from env variable to avoid exposing this via the admin-console
        String encryptionKey = getConfigProperty(context, ENCRYPTION_KEY, "changeme");
        String sessionReferenceData;
        String key = encryptionKey + encryptedSessionReferenceSalt;
        try {
            sessionReferenceData = CryptoUtil.decrypt(encryptedSessionReferenceData, key);
        } catch (Exception ex) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            log.infof("Reject session propagation. Reason: bad encryptedSessionReferenceData.");
            return;
        }

        String[] items = sessionReferenceData != null ? sessionReferenceData.split(";") : new String[0];
        if (items.length != 2) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            log.infof("Reject session propagation. Reason: bad sessionReferenceData.");
            return;
        }

        long timestamp = Long.parseLong(items[0]);

        int sessionReferenceMaxAgeSeconds = Integer.parseInt(getConfigProperty(context, SESSION_REFERENCE_MAX_AGE_SECONDS, "30"));
        boolean sessionReferenceToOld = Instant.now().isAfter(Instant.ofEpochMilli(timestamp).plus(sessionReferenceMaxAgeSeconds, ChronoUnit.SECONDS));
        if (sessionReferenceToOld) {
            context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
            log.infof("Reject session propagation. Reason: session reference to old.");
            return;
        }

        String sessionHandle = items[1];

        KeycloakSessionInfo keycloakSessionInfo = resolveKeycloakSessionId(sessionHandle, key, getConfigProperty(context, SESSION_VALIDATION_SERVICE_URL, null));
        if (keycloakSessionInfo == null) {
            context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
            log.infof("Reject session propagation. Reason: Remote session not found.");
            return;
        }

        String keycloakSessionId = keycloakSessionInfo.getKeycloakSessionId();

        RealmModel realm = context.getRealm();
        UserSessionModel userSession = session.sessions().getUserSession(realm, keycloakSessionId);

        if (userSession == null) {
            context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
            log.infof("Reject session propagation. Reason: keycloak session not found.");
            return;
        }

        if (!keycloakSessionInfo.getUsername().equals(userSession.getUser().getUsername())) {
            context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
            log.infof("Reject session propagation. Reason: username mismatch.");
            return;
        }

        // TODO check if session propagation is allowed for client...

        log.infof("Successful user session propagation.");
        context.getAuthenticationSession().setAuthenticatedUser(userSession.getUser());

        context.success();
    }

    private String getConfigProperty(AuthenticationFlowContext context, String key, String defaultValue) {

        if (context.getAuthenticatorConfig() == null) {
            return defaultValue;
        }

        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        if (config == null) {
            return defaultValue;
        }

        return config.getOrDefault(key, defaultValue);
    }

    private KeycloakSessionInfo resolveKeycloakSessionId(String sessionHandle, String key, String sessionValidationServiceUrl) {

        URI targetUri = UriBuilder.fromUri(sessionValidationServiceUrl).build(sessionHandle);
        SimpleHttp httpCall = SimpleHttp.doGet(targetUri.toString(), session);
        try {
            log.errorf("About to retrieve keycloak session id from backend.");
            SimpleHttp.Response response = httpCall.asResponse();
            log.infof("Retrieved keycloak session id from backend.");

            String encryptedJson = response.asString();
            String json = CryptoUtil.decrypt(encryptedJson, key);

            return JsonSerialization.readValue(json, KeycloakSessionInfo.class);
        } catch (IOException e) {
            log.errorf("Could not resolve keycloak session id from backend.", e);
        }

        return null;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    @Data
    static class KeycloakSessionInfo {

        String username;

        String keycloakSessionId;
    }
}
