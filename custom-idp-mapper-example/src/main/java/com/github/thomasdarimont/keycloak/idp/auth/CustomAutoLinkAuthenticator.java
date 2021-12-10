package com.github.thomasdarimont.keycloak.idp.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpAutoLinkAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Map;

@JBossLog
public class CustomAutoLinkAuthenticator extends IdpAutoLinkAuthenticator {

    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        String attribute = brokerContext.getIdpConfig().getAlias() + ":userId";

//        Object userInfo = brokerContext.getContextData().get("UserInfo");
//        if (userInfo != null) {
//            ((ObjectNode)userInfo).get
//        }

        String brokerUserId = brokerContext.getId();
        if (!brokerUserId.startsWith("f:")) {

            FederatedIdentityModel socialLink = new FederatedIdentityModel(brokerContext.getIdpConfig().getAlias(), brokerUserId, brokerContext.getEmail());
            UserModel alreadyLinkedUser = session.userLocalStorage().getUserByFederatedIdentity(realm, socialLink);

            if (alreadyLinkedUser != null) {
                context.setUser(alreadyLinkedUser);
                context.success();
                return;
            }

            context.attempted();
            return;
        }

        String[] userIds = brokerUserId.split(":");
        String idpUserId = userIds[1];
        String localUserId = userIds[2];


        UserModel existingUser = session.userLocalStorage().getUserById(realm, localUserId);
        if (existingUser == null) {
            context.attempted();
            return;
        }

        // add link to federated identity
//        FederatedIdentityModel socialLink = new FederatedIdentityModel(brokerContext.getIdpConfig().getAlias(), idpUserId, brokerContext.getEmail(), null);
//        session.userLocalStorage().addFederatedIdentity(realm, existingUser, socialLink);
        brokerContext.setId(idpUserId);
        brokerContext.setUsername(brokerContext.getEmail());

        log.debugf("User '%s' is set to authentication context when link with identity provider '%s' . Identity provider username is '%s' ", existingUser.getUsername(), brokerContext.getIdpConfig().getAlias(), brokerContext.getUsername());


        existingUser.setSingleAttribute(attribute, idpUserId);

        context.setUser(existingUser);
        context.success();
    }


}
