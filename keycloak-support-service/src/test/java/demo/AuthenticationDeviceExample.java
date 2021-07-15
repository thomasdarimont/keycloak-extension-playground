package demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class AuthenticationDeviceExample {

    public static void main(String[] args) {

        String clientId ="";
        String clientSecret  ="";

        var rt = new RestTemplate();
        rt.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("login_hint", "tester");
        requestBody.add("scope", "openid profile");
        requestBody.add("binding_message", "Allow order for XXX " + System.currentTimeMillis());
        requestBody.add("acr_values", "ACR1");
        // NOT Supported by Keycloak:
        //        requestBody.add("client_notification_token", "some_cnt");
//        requestBody.add("user_code", "user_code_42");

        var request = new HttpEntity<>(requestBody, headers);

        String cibaEndpoint = "http://localhost:8081/auth/realms/ciba-demo/protocol/openid-connect/ext/ciba/auth";
        var response = rt.postForEntity(cibaEndpoint, request, Map.class);
    }
}
