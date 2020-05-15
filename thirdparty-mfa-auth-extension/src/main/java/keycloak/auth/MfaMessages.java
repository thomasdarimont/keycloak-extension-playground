package keycloak.auth;

import lombok.extern.jbosslog.JBossLog;

import java.util.Properties;

@JBossLog
public class MfaMessages {

    private static final Properties MESSAGES;

    static {
        MESSAGES = new Properties();
        try {
            MESSAGES.load(MfaMessages.class.getClassLoader().getResourceAsStream("messages.properties"));
        } catch (Exception ex) {
            log.warnf("Could not load message properties");
        }
    }

    public static Properties getMessages() {
        return new Properties(MESSAGES);
    }
}
