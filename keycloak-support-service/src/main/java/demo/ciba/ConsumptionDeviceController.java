package demo.ciba;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@RestController
class ConsumptionDeviceController {

    /**
     * <pre>
     * curl -v -H "content-type: application/json" -d '{"orderItem":4711}' http://localhost:7777/process_order
     * </pre>
     *
     * @param orderRequest
     * @return
     */
    @PostMapping("/process_order")
    ResponseEntity<?> processOrderRequest(@RequestBody Map<String, Object> orderRequest) {

        String clientId = "app-consumption-device";
        String clientSecret = "9194e076-ea60-4939-8b69-417d5e7a3080";
        String cibaCallback = "http://localhost:7777/ciba/callback";

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

        System.out.println(response.getBody());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/ciba/callback")
    ResponseEntity<?> cibaCallback(@RequestBody Map<String, Object> cibaCallbackData) {


        return ResponseEntity.created(
                URI.create("http://foo")).build();
    }


    @PostMapping("/ciba/auth")
    ResponseEntity<?> appAuthEndpoint(@RequestBody Map<String, Object> authData) {

        System.out.println(authData);

        return ResponseEntity.created(
                URI.create("http://foo")).build();
    }
}
