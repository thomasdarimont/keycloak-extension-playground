package com.github.thomasdarimont.keycloak.auth.dynamicidp;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.constants.AdapterConstants;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientSessionCode;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@JBossLog
public class DynamicIdpRedirectAuthenticator implements Authenticator {

    public static final String TARGET_IDP_ATTRIBUTE = "targetIdp";

    public static final String EMAIL_TO_IDP_MAPPING_CONFIG_PROPERTY = "email-to-idp-mapping";

    public static final String FALLBACK_TO_AUTHFLOW_CONFIG_PROPERTY = "fallback-to-authflow";

    private final KeycloakSession session;

    public DynamicIdpRedirectAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        UserModel user = context.getUser();
        if (user == null) {
            context.attempted();
            return;
        }

        String targetIdp = determineTargetIdp(user, context);
        if (targetIdp != null) {
            redirect(context, targetIdp);
            return;
        }

        boolean fallbackToAuthFlow = getConfigValueOrDefault(context.getAuthenticatorConfig(), FALLBACK_TO_AUTHFLOW_CONFIG_PROPERTY, "true", Boolean::parseBoolean);
        if (fallbackToAuthFlow) {
            context.success();
            return;
        }

        context.getEvent().error(Errors.UNKNOWN_IDENTITY_PROVIDER);
        context.failure(AuthenticationFlowError.IDENTITY_PROVIDER_NOT_FOUND);
        context.cancelLogin();
        context.resetFlow();
    }

    protected void redirect(AuthenticationFlowContext context, String providerId) {

        IdentityProviderModel identityProviderModel = selectIdp(context, providerId);
        if (identityProviderModel == null || !identityProviderModel.isEnabled()) {
            log.warnf("Provider not found or not enabled for realm %s", providerId);
            context.attempted();
            return;
        }

        String accessCode = new ClientSessionCode<>(context.getSession(), context.getRealm(), context.getAuthenticationSession()).getOrGenerateCode();
        String clientId = context.getAuthenticationSession().getClient().getClientId();
        String tabId = context.getAuthenticationSession().getTabId();
        URI location = Urls.identityProviderAuthnRequest(context.getUriInfo().getBaseUri(), providerId, context.getRealm().getName(), accessCode, clientId, tabId);
        if (context.getAuthenticationSession().getClientNote(OAuth2Constants.DISPLAY) != null) {
            location = UriBuilder.fromUri(location).queryParam(OAuth2Constants.DISPLAY, context.getAuthenticationSession().getClientNote(OAuth2Constants.DISPLAY)).build();
        }
        log.debugf("Redirecting to %s", providerId);
        Response response = Response.seeOther(location).build();
        context.forceChallenge(response);
    }

    private IdentityProviderModel selectIdp(AuthenticationFlowContext context, String providerId) {

        var found = context.getRealm().getIdentityProvidersStream().filter(identityProvider->{
            if (!identityProvider.isEnabled()) {
                return false;
            }

            return providerId.equals(identityProvider.getAlias());
        }).findFirst();

        return found.orElse(null);
    }


    protected String determineTargetIdp(UserModel user, AuthenticationFlowContext context) {

        String targetIdp = determineTargetIdpFromUrlParameter(context);
        if (targetIdp != null) {
            return targetIdp;
        }

        targetIdp = determineTargetIdpViaAttribute(user);
        if (targetIdp != null) {
            return targetIdp;
        }

        log.warnf("Target IDP: "+targetIdp);

        return determineTargetIdpViaUserEmail(user, context);
    }

    protected String determineTargetIdpFromUrlParameter(AuthenticationFlowContext context) {
        return context.getUriInfo().getQueryParameters().getFirst(AdapterConstants.KC_IDP_HINT);
    }

    protected String determineTargetIdpViaAttribute(UserModel user) {
        return user.getFirstAttribute(TARGET_IDP_ATTRIBUTE);
    }

    protected String determineTargetIdpViaUserEmail(UserModel user, AuthenticationFlowContext context) {

        String email = user.getEmail();
        if (email == null) {
            return null;
        }

        String mappingString = getConfigValueOrDefault(context.getAuthenticatorConfig(), EMAIL_TO_IDP_MAPPING_CONFIG_PROPERTY, "", String::valueOf);
        String[] mappings = mappingString.split(";");
        for (String mapping : mappings) {
            String[] emailSuffixPatternToIdpId = mapping.split("/");

            if(emailSuffixPatternToIdpId.length!=2){
                return null;
            }

            String emailSuffixPattern = emailSuffixPatternToIdpId[0];
            String idpId = emailSuffixPatternToIdpId[1];

            if (email.matches(emailSuffixPattern)) {
                return idpId;
            }
        }

        return null;
    }

    protected <T> T getConfigValueOrDefault(AuthenticatorConfigModel configModel, String key, String defaultValue, Function<String, T> converter) {

        if (configModel == null) {
            return converter.apply(defaultValue);
        }

        Map<String, String> config = configModel.getConfig();
        if (config == null || config.isEmpty()) {
            return converter.apply(defaultValue);
        }

        return converter.apply(config.getOrDefault(key, defaultValue));
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
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
