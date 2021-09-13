package com.github.thomasdarimont.keycloak.motd;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;

import java.util.Locale;
import java.util.Map;

public class SimpleMotdMessageProvider implements MotdMessageProvider {

    @Override
    public MotdMessage getMessage(KeycloakSession session, Map<String, String> config) {

        KeycloakContext context = session.getContext();

        // TODO use locale for i18n

        // Pull message from theme
        Locale locale = context.resolveLocale(context.getAuthenticationSession().getAuthenticatedUser());
//        Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
//        Properties messages = theme.getMessages(locale);

        // compute message from config

        String messageHeader = "Message Header " + locale.getLanguage();
        String messageDetail = "Message Detail " + locale.getLanguage();

        return new MotdMessage(messageHeader, messageDetail);
    }
}
