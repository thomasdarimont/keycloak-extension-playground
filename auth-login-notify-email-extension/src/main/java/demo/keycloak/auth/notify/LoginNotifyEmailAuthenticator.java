package demo.keycloak.auth.notify;

import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@JBossLog
public class LoginNotifyEmailAuthenticator implements Authenticator {

    static final String EMAIL_TEMPLATE_NAME = "emailTemplateName";
    static final String TIME_SINCE_LAST_LOGIN = "timeSinceLastLogin";

    private final KeycloakSession session;

    public LoginNotifyEmailAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        UserModel user = context.getUser();

        long currentLoginTime = System.currentTimeMillis();
        long lastLoginTime = detectLastLoginTimeForUser(user, currentLoginTime);

        try {
            if (user.getEmail() != null) {
                String timeSinceLastEmail = getConfigSettingOrDefault(context, TIME_SINCE_LAST_LOGIN, null);
                if (timeSinceLastEmail != null) {
                    Duration duration = Duration.parse(timeSinceLastEmail);
                    Instant lastLogin = Instant.ofEpochMilli(lastLoginTime);
                    Instant currentLogin = Instant.ofEpochMilli(currentLoginTime);
                    if (lastLogin.plus(duration).isBefore(currentLogin)) {
                        log.infof("Sending login notification email after longer absence. userId=%s", user.getUsername());
                        sendLoginNotificationEmail(context, user);
                    }
                }
            }
        } catch (Exception ex) {
            log.warnf("Could not send login notification email after longer absence. userId=%s", user.getId(), ex);
        } finally {
            updateLastLoginTimeForUser(user, currentLoginTime);
            context.success();
        }
    }

    protected void updateLastLoginTimeForUser(UserModel user, long currentLoginTime) {
        user.setSingleAttribute("lastLoginTime", String.valueOf(currentLoginTime));
    }

    protected long detectLastLoginTimeForUser(UserModel user, long currentLoginTime) {

        long lastLoginTime = currentLoginTime;

        String lastLoginTimeString = user.getFirstAttribute("lastLoginTime");
        if (lastLoginTimeString != null) {
            lastLoginTime = Long.parseLong(lastLoginTimeString);
        }

        return lastLoginTime;
    }

    protected void sendLoginNotificationEmail(AuthenticationFlowContext context, UserModel user) throws EmailException {

        String templateName = getConfigSettingOrDefault(context, EMAIL_TEMPLATE_NAME, null);
        if (templateName == null) {
            return;
        }

        Map<String, String> smtpConfig = context.getRealm().getSmtpConfig();
        if (smtpConfig == null || smtpConfig.isEmpty()) {
            return;
        }
        EmailSenderProvider emailSender = session.getProvider(EmailSenderProvider.class);
        Locale locale = session.getContext().resolveLocale(user);

        Map<String, String> attributes = createMailAttributes(context);

        ResourceBundle msg = LoginNotifyEmailMessages.getMessages(locale);
        String subject = replace(msg.getString(templateName + "Subject"), attributes);

        // TODO externalize textBody and htmlBody to dedicated templates
        String textBody = replace(msg.getString(templateName + "Text"), attributes);
        String htmlBody = StringEscapeUtils.unescapeJava(replace(msg.getString(templateName + "Html"), attributes));

        emailSender.send(smtpConfig, user, subject, textBody, htmlBody);
    }

    private Map<String, String> createMailAttributes(AuthenticationFlowContext context) {

        RealmModel realm = context.getRealm();

        Map<String, String> attributes = new HashMap<>();
        attributes.put("username", context.getUser().getUsername());
        attributes.put("ipAddress", context.getConnection().getRemoteAddr());
        URI accountUrl = context.getUriInfo().getRequestUriBuilder().replaceQuery("").replacePath("/auth/realms/{realm}/account").build(realm.getName());
        attributes.put("accountUrl", accountUrl.toString());
        String realmDisplayName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName();
        attributes.put("realmDisplayName", realmDisplayName);

        return attributes;
    }

    private String replace(String text, Map<String, String> attributes) {

        String result = text;
        result = text.replace("${username}", attributes.get("username"));
        result = result.replace("${ipAddress}", attributes.get("ipAddress"));
        result = result.replace("${accountUrl}", attributes.get("accountUrl"));
        result = result.replace("${realmDisplayName}", attributes.get("realmDisplayName"));

        return result;
    }

    private String getConfigSettingOrDefault(AuthenticationFlowContext context, String key, String defaultValue) {

        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        if (authenticatorConfig == null) {
            return defaultValue;
        }
        Map<String, String> config = authenticatorConfig.getConfig();
        if (config == null) {
            return defaultValue;
        }
        return config.getOrDefault(key, defaultValue);
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
        // NOOP
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
