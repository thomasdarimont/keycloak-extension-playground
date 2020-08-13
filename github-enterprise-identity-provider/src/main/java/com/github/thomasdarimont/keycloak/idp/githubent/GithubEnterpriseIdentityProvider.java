package com.github.thomasdarimont.keycloak.idp.githubent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.social.github.GitHubIdentityProvider;

import java.util.Iterator;

public class GithubEnterpriseIdentityProvider extends GitHubIdentityProvider {

    public GithubEnterpriseIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
        super(session, config);
    }

    void patch(OAuth2IdentityProviderConfig config) {

        // fix the broken configuration from GitHubIdentityProvider
        getConfig().setAuthorizationUrl(config.getAuthorizationUrl());
        getConfig().setTokenUrl(config.getTokenUrl());
        getConfig().setUserInfoUrl(config.getUserInfoUrl());
        getConfig().setUserInfoUrl(config.getUserInfoUrl());
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return getConfig().getUserInfoUrl();
    }


    // needed to copy those methods from GitHubIdentityProvider as there were private and did not allow proper extension...
    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            JsonNode profile = SimpleHttp.doGet(getConfig().getUserInfoUrl(), session).header("Authorization", "Bearer " + accessToken).asJson();

            BrokeredIdentityContext user = extractIdentityFromProfile(null, profile);

            if (user.getEmail() == null) {
                user.setEmail(searchEmail(accessToken));
            }

            return user;
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from github.", e);
        }
    }

    private String searchEmail(String accessToken) {
        try {
            ArrayNode emails = (ArrayNode) SimpleHttp.doGet(getConfig().getConfig().get("emailUrl"), session).header("Authorization", "Bearer " + accessToken).asJson();

            Iterator<JsonNode> loop = emails.elements();
            while (loop.hasNext()) {
                JsonNode mail = loop.next();
                if (mail.get("primary").asBoolean()) {
                    return getJsonProperty(mail, "email");
                }
            }
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user email from github.", e);
        }
        throw new IdentityBrokerException("Primary email from github is not found.");
    }
}
