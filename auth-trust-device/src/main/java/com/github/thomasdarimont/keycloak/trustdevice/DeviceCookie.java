package com.github.thomasdarimont.keycloak.trustdevice;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;

public class DeviceCookie {

    public static final String COOKIE_NAME = "KEYCLOAK_DEVICE";

    public static void removeDeviceCookie(KeycloakSession session, RealmModel realm) {

        UriBuilder baseUriBuilder = session.getContext().getUri().getBaseUriBuilder();
        String realmPath = baseUriBuilder.path("realms").path(realm.getName()).path("/").build().getPath();

        NewCookie cookie = new NewCookie(COOKIE_NAME, ""
                , realmPath
                , null // domain
                , null // comment
                , 0
                , false // secure FIXME: true
                , true // httponly
        );

        HttpResponse httpResponse = ResteasyProviderFactory.getContextData(HttpResponse.class);
        httpResponse.addNewCookie(cookie);
    }

    public static void addDeviceCookie(String deviceTokenString, KeycloakSession session, RealmModel realm) {

        NewCookie newCookie = DeviceCookie.generateTrustedDeviceCookie(deviceTokenString, session, realm);
        HttpResponse httpResponse = ResteasyProviderFactory.getContextData(HttpResponse.class);
        httpResponse.addNewCookie(newCookie);
    }

    public static NewCookie generateTrustedDeviceCookie(String deviceTokenString, KeycloakSession session, RealmModel realm) {

        UriBuilder baseUriBuilder = session.getContext().getUri().getBaseUriBuilder();
        String realmPath = baseUriBuilder.path("realms").path(realm.getName()).path("/").build().getPath();

        int numberOfDaysToTrustDevice = 120; //FIXME make name of days to remember deviceToken configurable
        NewCookie cookie = new NewCookie(COOKIE_NAME, deviceTokenString
                , realmPath
                , null // domain
                , null // comment
                , Time.currentTime() + numberOfDaysToTrustDevice * 24 * 60 + 60 // max age
                , false // secure FIXME: true
                , true // httponly
        );

        return cookie;
    }

    public static DeviceToken readDeviceTokenFromCookie(HttpRequest httpRequest, KeycloakSession session) {

        Cookie deviceCookie = httpRequest.getHttpHeaders().getCookies().get(COOKIE_NAME);
        if (deviceCookie == null) {
            return null;
        }

        // decodes and validates device cookie
        DeviceToken deviceToken = session.tokens().decode(deviceCookie.getValue(), DeviceToken.class);

        return deviceToken;
    }
}
