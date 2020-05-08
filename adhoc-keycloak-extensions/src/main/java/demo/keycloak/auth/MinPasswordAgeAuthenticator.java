package demo.keycloak.auth;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JBossLog
public class MinPasswordAgeAuthenticator implements Authenticator {


    static final String MIN_PASSWORD_AGE_DURATION = "minPasswordAgeDuration";

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        Map<String, String> config = (context.getAuthenticatorConfig() == null ? Collections.emptyMap() : context.getAuthenticatorConfig().getConfig());

        List<CredentialModel> passwords = context.getSession().userCredentialManager().getStoredCredentialsByType(realm, user, PasswordCredentialModel.TYPE);
        if (!passwords.isEmpty()) {
            CredentialModel passwordCredential = passwords.get(0);

            Instant creationTime = Instant.ofEpochMilli(passwordCredential.getCreatedDate());

            Duration minPasswordAge = Duration.parse(config.getOrDefault(MIN_PASSWORD_AGE_DURATION, "PT15M"));

            if (creationTime.isAfter(Instant.now().minus(minPasswordAge))) {

                log.warnf("Access denied because of min password age. realm=%s username=%s", realm.getName(), user.getUsername());
                context.getEvent().user(user);
                context.getEvent().error(Errors.NOT_ALLOWED);
                context.forkWithErrorMessage(new FormMessage(Messages.NO_ACCESS));

                return;
            }
        }

        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {
        // NOOP
    }
}
