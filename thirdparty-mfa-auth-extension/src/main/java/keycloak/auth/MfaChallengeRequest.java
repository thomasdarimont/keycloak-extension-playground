package keycloak.auth;

import lombok.Data;

@Data
class MfaChallengeRequest {

    private String username;

    private MfaMethod mfaMethod = MfaMethod.PUSH;

}

