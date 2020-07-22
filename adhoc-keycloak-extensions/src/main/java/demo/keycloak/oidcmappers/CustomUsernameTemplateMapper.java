package demo.keycloak.oidcmappers;

import com.google.auto.service.AutoService;
import org.keycloak.broker.oidc.mappers.UsernameTemplateMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

@AutoService(IdentityProviderMapper.class)
public class CustomUsernameTemplateMapper extends UsernameTemplateMapper {

    public static final String PROVIDER_ID = "custom-oidc-username-idp-mapper";

    @Override
    public String getDisplayType() {
        return "Custom Username Template Importer";
    }

    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        super.preprocessFederatedIdentity(session, realm, mapperModel, context);
        String username = context.getUsername();

        if (username.contains("@")) {
            username = username.substring(0, username.indexOf('@'));
        }

        context.setModelUsername(username);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
