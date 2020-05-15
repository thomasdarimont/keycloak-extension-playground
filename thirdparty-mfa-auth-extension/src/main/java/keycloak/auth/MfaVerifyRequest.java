package keycloak.auth;

import lombok.Data;

import java.util.UUID;

@Data
class MfaVerifyRequest {

    private UUID challengeId;

    private String challengeInput;
}

