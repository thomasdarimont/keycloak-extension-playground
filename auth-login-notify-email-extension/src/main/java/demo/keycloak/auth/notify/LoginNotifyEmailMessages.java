package demo.keycloak.auth.notify;

import lombok.extern.jbosslog.JBossLog;

import java.util.Locale;
import java.util.ResourceBundle;

@JBossLog
public class LoginNotifyEmailMessages {

    public static ResourceBundle getMessages(Locale locale) {
        return ResourceBundle.getBundle("messages", locale);
    }
}
