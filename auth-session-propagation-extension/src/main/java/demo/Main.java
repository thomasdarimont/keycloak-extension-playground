package demo;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws Exception {

        String serverUrl = "http://127.0.0.1:8081/auth";
        String realm = "session-propagation";
        String username = "tester";
        String password = "test";
        String clientId = "app-backend";
        String clientSecret = "0b69f10f-ba95-4674-a2d0-62a7d6ae60f7";

        Keycloak keycloak = Keycloak.getInstance(serverUrl, realm, username, password, clientId, clientSecret);
        AccessTokenResponse accessToken = keycloak.tokenManager().getAccessToken();
        String sessionState = accessToken.getSessionState();
        System.out.println(sessionState);

        while (true) {
            Thread.sleep(2500);
            Map<Object, Object> data = new HashMap<>();
            data.put(OAuth2Constants.CLIENT_ID, clientId);
            data.put(OAuth2Constants.CLIENT_SECRET, clientSecret);
            data.put("token", keycloak.tokenManager().getAccessTokenString());

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s/realms/%s/protocol/openid-connect/token/introspect", serverUrl, realm)))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(ofFormData(data))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse.BodyHandler<String> asString = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = client.send(postRequest, asString);

            System.out.printf("%s: %s %n", Instant.now(), response.body());
        }
    }

    // Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
