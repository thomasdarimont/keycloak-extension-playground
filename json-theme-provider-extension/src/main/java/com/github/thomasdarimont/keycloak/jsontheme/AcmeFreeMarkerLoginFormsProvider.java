package com.github.thomasdarimont.keycloak.jsontheme;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationSelectionOption;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.AuthenticationContextBean;
import org.keycloak.forms.login.freemarker.model.ClientBean;
import org.keycloak.forms.login.freemarker.model.RealmBean;
import org.keycloak.forms.login.freemarker.model.UrlBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;
import org.keycloak.utils.MediaType;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@JBossLog
public class AcmeFreeMarkerLoginFormsProvider extends FreeMarkerLoginFormsProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(VisibilityChecker.Std.defaultInstance()
                    .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                    .withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY))
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private static final String ACME_JSON_THEME = "acme-json";

    public AcmeFreeMarkerLoginFormsProvider(KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);
    }

    @Override
    protected Response processTemplate(Theme theme, String templateName, Locale locale) {

        if (!theme.getName().equals(ACME_JSON_THEME)) {
            return super.processTemplate(theme, templateName, locale);
        }

        try {
            Map<String, Object> filteredAttributes = new HashMap<>(attributes);
            if (filteredAttributes.containsKey("auth")) {
                filteredAttributes.put("auth", new AuthenticationContextBeanAdapter((AuthenticationContextBean) attributes.get("auth")));
            }

            if (filteredAttributes.containsKey("url")) {
                filteredAttributes.put("url", new UrlBeanAdapter((UrlBean) attributes.get("url")));
            }

            if (filteredAttributes.containsKey("realm")) {
                filteredAttributes.put("realm", new RealmBeanAdapter((RealmBean) attributes.get("realm")));
            }

            if (filteredAttributes.containsKey("client")) {
                filteredAttributes.put("client", new ClientBeanAdapter((ClientBean) attributes.get("client")));
            }

            filteredAttributes.remove("properties");
            filteredAttributes.remove("locale");

            String templateJson = OBJECT_MAPPER.writeValueAsString(Map.of("templateName", templateName, "attributes", filteredAttributes));
            System.out.printf("%s%n", templateJson);

            javax.ws.rs.core.MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
            Response.ResponseBuilder builder = Response.status(status == null ? Response.Status.OK : status)
                    .type(mediaType)
                    .language(locale)
                    .entity(templateJson);
            for (Map.Entry<String, String> entry : httpResponseHeaders.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
            return builder.build();

        } catch (Exception e) {
            log.error("Failed to process template", e);
            return Response.serverError().build();
        }
    }

    public static class AuthenticationContextBeanAdapter {

        private final AuthenticationContextBean auth;

        public AuthenticationContextBeanAdapter(AuthenticationContextBean auth) {
            this.auth = auth;
        }

        public List<AuthenticationSelectionOption> getAuthenticationSelections() {
            return auth.getAuthenticationSelections();
        }

        public boolean showTryAnotherWayLink() {
            return auth.showTryAnotherWayLink();
        }


        public boolean showUsername() {
            return auth.showUsername();
        }

        public boolean showResetCredentials() {
            return auth.showResetCredentials();
        }

        public String getAttemptedUsername() {
            try {
                return auth.getAttemptedUsername();
            } catch (NullPointerException npe) {
                return null;
            }
        }
    }

    public static class UrlBeanAdapter {

        private final UrlBean urlBean;

        public UrlBeanAdapter(UrlBean urlBean) {
            this.urlBean = urlBean;
        }

        public String getLoginUrl() {
            return urlBean.getLoginUrl();
        }

        public String getLoginRestartFlowUrl() {
            return urlBean.getLoginRestartFlowUrl();
        }

        public String getRegistrationAction() {
            return urlBean.getRegistrationAction();
        }

        public String getRegistrationUrl() {
            return urlBean.getRegistrationUrl();
        }

        public String getFirstBrokerLoginUrl() {
            return urlBean.getFirstBrokerLoginUrl();
        }

        public String getLoginAction() {
            return urlBean.hasAction() ? urlBean.getLoginAction() : null;
        }
    }

    public static class RealmBeanAdapter {

        private final RealmBean realmBean;

        public RealmBeanAdapter(RealmBean realmBean) {
            this.realmBean = realmBean;
        }

        public String getName() {
            return realmBean.getName();
        }

        public String getDisplayName() {
            return realmBean.getDisplayName();
        }
    }

    public static class ClientBeanAdapter {

        private final ClientBean clientBean;

        public ClientBeanAdapter(ClientBean clientBean) {
            this.clientBean = clientBean;
        }

        public String getClientId() {
            return clientBean.getClientId();
        }

        public String getName() {
            return clientBean.getName();
        }

        public String getDescription() {
            return clientBean.getDescription();
        }

        public String getBaseUrl() {
            return clientBean.getBaseUrl();
        }
    }
}
