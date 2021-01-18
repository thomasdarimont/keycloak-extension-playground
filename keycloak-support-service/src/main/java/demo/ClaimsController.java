package demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
class ClaimsController {

    @GetMapping("/claims")
    Object getCustomClaims(@RequestParam String userId, @RequestParam String username, @RequestParam(required = false) String clientId, @RequestParam(required = false) String issuer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("values1", Arrays.asList("value1a", "value1b"));
        claims.put("values2", Arrays.asList("value2a", "value2b"));
        claims.put("uid", UUID.randomUUID());
        claims.put("bubu", "Bubu");
        claims.put("username", username);
        claims.put("clientId", clientId);
        claims.put("issuer", issuer);
        claims.put("userId", userId);
        claims.put("roles", Map.of("dms", List.of("ROLE1", "ROLE2")));
        return claims;
    }
}
