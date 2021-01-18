package demo;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@RestController
class ClientStorageController {

    private static final List<CustomClient> CLIENTS = new ArrayList<>(List.of(
            new CustomClient(
                    "f87f9f75-71ac-49da-a6ec-48171fa937ea",
                    "custom-client-1",
                    "Custom Client 1",
                    "secret",
                    Set.of("profile"),
                    Set.of("email"),
                    true
            ),
            new CustomClient(
                    "7c22a7f4-7b91-40c5-b695-4084df005d36",
                    "custom-client-2",
                    "Custom Client 2",
                    "secret",
                    Set.of("profile"),
                    Set.of(),
                    true
            )
    ));

    @GetMapping("/clients/search/by-client-id/{clientId}")
    CustomClient getClientByClientId(@PathVariable("clientId") String clientId) {
        return lookupBy(clientId, CustomClient::getClientId);
    }

    @GetMapping("/clients/{id}")
    CustomClient getClientById(@PathVariable("id") String id) {
        return lookupBy(id, CustomClient::getId);
    }

    private <T> CustomClient lookupBy(T value, Function<CustomClient, T> extractor) {
        for (var client : CLIENTS) {
            if (extractor.apply(client).equals(value)) {
                return client;
            }
        }
        return null;
    }

    @Data
    public static class CustomClient {

        private final String id;

        private final String clientId;

        private final String name;

        private final String secret;

        private final Set<String> defaultScopes;

        private final Set<String> optionalScopes;

        private final boolean enabled;
    }
}
