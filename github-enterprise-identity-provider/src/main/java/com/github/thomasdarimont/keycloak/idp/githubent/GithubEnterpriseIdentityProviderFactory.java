package com.github.thomasdarimont.keycloak.idp.githubent;

import com.google.auto.service.AutoService;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.social.github.GitHubIdentityProvider;
import org.keycloak.social.github.GitHubIdentityProviderFactory;

@AutoService(SocialIdentityProviderFactory.class)
public class GithubEnterpriseIdentityProviderFactory extends GitHubIdentityProviderFactory {

    public static final String PROVIDER_ID = "github-enterprise";

    @Override
    public String getName() {
        return "GitHub Enterprise";
    }

    @Override
    public GitHubIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        OAuth2IdentityProviderConfig config = new OAuth2IdentityProviderConfig(model);
        GithubEnterpriseIdentityProvider github = new GithubEnterpriseIdentityProvider(session, config);
        github.patch(config);
        return github;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
