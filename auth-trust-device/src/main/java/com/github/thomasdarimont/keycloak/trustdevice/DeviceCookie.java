package com.github.thomasdarimont.keycloak.trustdevice;

import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.ServerCookie;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.util.CookieHelper;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.UriBuilder;

public class DeviceCookie {

    public static final String COOKIE_NAME = "KEYCLOAK_DEVICE";

    public static void removeDeviceCookie(KeycloakSession session, RealmModel realm) {
        // maxAge = 1 triggers legacy cookie removal
        addCookie("", session, realm, 1);
    }

    public static void addDeviceCookie(String deviceTokenString, int maxAge, KeycloakSession session, RealmModel realm) {
        addCookie(deviceTokenString, session, realm, maxAge);
    }

    private static void addCookie(String deviceTokenString, KeycloakSession session, RealmModel realm, int maxAge) {

        UriBuilder baseUriBuilder = session.getContext().getUri().getBaseUriBuilder();
        // TODO think about narrowing the cookie-path to only contain the /auth path.
        String path = baseUriBuilder.path("realms").path(realm.getName()).path("/").build().getPath();

        ClientConnection connection = session.getContext().getConnection();
        boolean secure = realm.getSslRequired().isRequired(connection);

        ServerCookie.SameSiteAttributeValue sameSiteValue = secure ? ServerCookie.SameSiteAttributeValue.NONE : null;
        CookieHelper.addCookie(
                COOKIE_NAME,
                deviceTokenString,
                path,
                null,// domain
                null, // comment
                maxAge,
                secure,
                true, // httponly
                sameSiteValue
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
