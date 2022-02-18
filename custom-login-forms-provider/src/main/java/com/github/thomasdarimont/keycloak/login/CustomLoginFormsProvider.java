package com.github.thomasdarimont.keycloak.login;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;

import javax.ws.rs.core.Response;
import java.util.Locale;

@JBossLog
public class CustomLoginFormsProvider extends FreeMarkerLoginFormsProvider {

    public CustomLoginFormsProvider(KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);
    }

    protected Response processTemplate(Theme theme, String templateName, Locale locale) {
        // add your custom data to attributes
        attributes.put("mykey", "hello");
        log.infof("Rendering theme=%s templateName=%s", theme.getName(), templateName);
        return super.processTemplate(theme, templateName, locale);
    }

}
