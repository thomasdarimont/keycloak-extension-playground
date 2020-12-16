package demo.keycloak.oidcmappers;

import com.google.auto.service.AutoService;
import org.jboss.logging.Logger;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserPropertyMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.util.List;

@AutoService(ProtocolMapper.class)
public class CrossRealmClientAuthMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String PROVIDER_ID = "oidc-cross-realm-auth-protocol-mapper";

    private static final Logger LOGGER = Logger.getLogger(RemoteOidcMapper.class);

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {

        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .build();

        OIDCAttributeMapperHelper.addAttributeConfig(CONFIG_PROPERTIES, UserPropertyMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Demo Cross Realm Auth Mapper";
    }

    @Override
    public String getHelpText() {
        return "Demo Cross Realm Auth Mapper";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {

        Object claimValue = "42";

        fetchCrossRealmData(keycloakSession);

        LOGGER.infof("setClaim %s=%s", mappingModel.getName(), claimValue);

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
    }

    private Object fetchCrossRealmData(KeycloakSession session) {


        RealmModel targetRealm = session.realms().getRealmByName("services-demo");
        ClientModel serviceClient = targetRealm.getClientByClientId("simple-service");
        if (!serviceClient.isServiceAccountsEnabled()) {
            return null;
        }
        String loopback = "127.0.0.1";
        EventBuilder event = new EventBuilder(targetRealm, session, new ClientConnection() {
            public String getRemoteAddr() {
                return loopback;
            }

            public String getRemoteHost() {
                return loopback;
            }

            public int getRemotePort() {
                return 8080;
            }

            public String getLocalAddr() {
                return loopback;
            }

            public int getLocalPort() {
                return 8080;
            }
        });

        UserModel serviceAccountUser = session.users().getServiceAccount(serviceClient);
        if (!serviceAccountUser.isEnabled()) {
            return null;
        }

        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(targetRealm, false);
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(serviceClient);
        authSession.setAuthenticatedUser(serviceAccountUser);
        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), targetRealm.getName()));
        authSession.setClientNote(OIDCLoginProtocol.SCOPE_PARAM, "openid profile roles");
        UserSessionModel serviceAccountUserSession = session.sessions().createUserSession(
                authSession.getParentSession().getId(), targetRealm, serviceAccountUser, serviceAccountUser.getUsername(),
                loopback, ServiceAccountConstants.CLIENT_AUTH, false, null, null, UserSessionModel.SessionPersistenceState.TRANSIENT);
        AuthenticationManager.setClientScopesInSession(authSession);
        ClientSessionContext clientSessionContext = TokenManager.attachAuthenticationSession(session, serviceAccountUserSession, authSession);

        // Notes about serviceClient details
        serviceAccountUserSession.setNote(ServiceAccountConstants.CLIENT_ID, serviceClient.getClientId());
        serviceAccountUserSession.setNote(ServiceAccountConstants.CLIENT_HOST, loopback);
        serviceAccountUserSession.setNote(ServiceAccountConstants.CLIENT_ADDRESS, loopback);

        TokenManager tokenManager = new TokenManager();
        TokenManager.AccessTokenResponseBuilder responseBuilder = tokenManager.responseBuilder(targetRealm, serviceClient, event, session, serviceAccountUserSession, clientSessionContext)
                .generateAccessToken();

        AccessTokenResponse accessTokenResponse = responseBuilder.build();

        String accessToken = accessTokenResponse.getToken();

        LOGGER.infof("AccessToken: %s", accessToken);

        return null;
    }
}