package demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
class AccessManagementController {

    @GetMapping("/am")
    Object getCustomClaims(@RequestParam String userId, @RequestParam String username, @RequestParam(required = false) String clientId, @RequestParam(required = false) String issuer) {

        Map<String, Object> claims = new HashMap<>();
//        claims.put("roles", Map.of("dms", List.of("ROLE1", "ROLE2")));

        Map<String, Map<String, List<String>>> clientRoleMapping = new HashMap<>(
                Map.of(
                        "app-service1", Map.of("roles", List.of("user", "admin")),
                        "app-service2", Map.of("roles", List.of("user")),
                        "app-service3", Map.of("roles", List.of("user", "support"))
                )
        );

        if ("demo-client-remote-claims".equals(clientId)) {
            clientRoleMapping.put(clientId, Map.of("roles", List.of("user")));
        }

        // client roles
        claims.put("resource_access", clientRoleMapping);

        // realm roles
        claims.put("realm_access", Map.of("roles", List.of("user", "admin")));

        claims.put("acme", Map.of("mandantId", 42));

        return claims;
    }
}
