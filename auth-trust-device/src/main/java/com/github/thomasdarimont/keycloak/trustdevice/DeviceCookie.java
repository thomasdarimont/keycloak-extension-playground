package com.github.thomasdarimont.keycloak.trustdevice;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;

public class DeviceCookie {

    public static final String COOKIE_NAME = "KEYCLOAK_DEVICE";

    public static void removeDeviceCookie(KeycloakSession session, RealmModel realm) {

        NewCookie cookie = generateCookie("", session, realm, 0);

        HttpResponse httpResponse = ResteasyProviderFactory.getContextData(HttpResponse.class);
        httpResponse.addNewCookie(cookie);
    }

    public static void addDeviceCookie(String deviceTokenString, int maxAge, KeycloakSession session, RealmModel realm) {

        NewCookie cookie = generateCookie(deviceTokenString, session, realm, maxAge);

        HttpResponse httpResponse = ResteasyProviderFactory.getContextData(HttpResponse.class);
        httpResponse.addNewCookie(cookie);
    }

    private static NewCookie generateCookie(String deviceTokenString, KeycloakSession session, RealmModel realm, int maxAge) {

        UriBuilder baseUriBuilder = session.getContext().getUri().getBaseUriBuilder();
        // TODO think about narrowing the cookie-path to only contain the /auth path.
        String path = baseUriBuilder.path("realms").path(realm.getName()).path("/").build().getPath();

        ClientConnection connection = session.getContext().getConnection();
        boolean secure = realm.getSslRequired().isRequired(connection);

        return new NewCookie(COOKIE_NAME, deviceTokenString
                , path
                , null // domain
                , null // comment
                , maxAge // max age
                , secure
                , true // httponly
        );
    }

    public static DeviceToken parseDeviceTokenFromCookie(HttpRequest httpRequest, KeycloakSession session) {

        Cookie deviceCookie = httpRequest.getHttpHeaders().getCookies().get(COOKIE_NAME);
        if (deviceCookie == null) {
            return null;
        }

        // decodes and validates device cookie
        return session.tokens().decode(deviceCookie.getValue(), DeviceToken.class);
    }
}
