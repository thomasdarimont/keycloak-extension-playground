package com.github.thomasdarimont.keycloak.auth.dynamicidp;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.constants.AdapterConstants;
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

@JBossLog
public class DynamicIdpRedirectAuthenticator implements Authenticator {

    public static final String TARGET_IDP_ATTRIBUTE = "targetIdp";

    static final String EMAIL_TO_IDP_MAPPING_CONFIG_PROPERTY = "email-to-idp-mapping";

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

        if (targetIdp == null) {
            context.attempted();
            return;
        }

        redirect(context, targetIdp);
    }

    private void redirect(AuthenticationFlowContext context, String providerId) {

        IdentityProviderModel identityProviderModel = selectIdp(context, providerId);
        if (identityProviderModel == null) {
            return;
        }

        String accessCode = new ClientSessionCode<>(context.getSession(), context.getRealm(), context.getAuthenticationSession()).getOrGenerateCode();
        String clientId = context.getAuthenticationSession().getClient().getClientId();
        String tabId = context.getAuthenticationSession().getTabId();
        URI location = Urls.identityProviderAuthnRequest(context.getUriInfo().getBaseUri(), providerId, context.getRealm().getName(), accessCode, clientId, tabId);
        if (context.getAuthenticationSession().getClientNote(OAuth2Constants.DISPLAY) != null) {
            location = UriBuilder.fromUri(location).queryParam(OAuth2Constants.DISPLAY, context.getAuthenticationSession().getClientNote(OAuth2Constants.DISPLAY)).build();
        }
        Response response = Response.seeOther(location).build();

        log.debugf("Redirecting to %s", providerId);
        context.forceChallenge(response);

        log.warnf("Provider not found or not enabled for realm %s", providerId);
        context.attempted();
    }

    private IdentityProviderModel selectIdp(AuthenticationFlowContext context, String providerId) {

        List<IdentityProviderModel> identityProviders = context.getRealm().getIdentityProviders();
        for (IdentityProviderModel identityProvider : identityProviders) {

            if (!identityProvider.isEnabled()) {
                continue;
            }

            if (providerId.equals(identityProvider.getAlias())) {
                return identityProvider;
            }
        }

        return null;
    }


    private String determineTargetIdp(UserModel user, AuthenticationFlowContext context) {

        String targetIdp = determineTargetIdpFromUrlParameter(context);
        if (targetIdp != null) {
            return targetIdp;
        }

        targetIdp = determineTargetIdpViaAttribute(user);
        if (targetIdp != null) {
            return targetIdp;
        }

        return determineTargetIdpViaUserEmail(user, context);
    }

    private String determineTargetIdpFromUrlParameter(AuthenticationFlowContext context) {
        return context.getUriInfo().getQueryParameters().getFirst(AdapterConstants.KC_IDP_HINT);
    }

    private String determineTargetIdpViaAttribute(UserModel user) {
        return user.getFirstAttribute(TARGET_IDP_ATTRIBUTE);
    }

    private String determineTargetIdpViaUserEmail(UserModel user, AuthenticationFlowContext context) {

        String email = user.getEmail();
        if (email == null) {
            return null;
        }

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        String mappingString = configModel.getConfig().get(EMAIL_TO_IDP_MAPPING_CONFIG_PROPERTY);
        String[] mappings = mappingString.split(";");
        for (String mapping : mappings) {
            String[] emailSuffixPatternToIdpId = mapping.split("/");
            String emailSuffixPattern = emailSuffixPatternToIdpId[0];
            String idpId = emailSuffixPatternToIdpId[1];

            if (email.matches(emailSuffixPattern)) {
                return idpId;
            }
        }

        return null;
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
