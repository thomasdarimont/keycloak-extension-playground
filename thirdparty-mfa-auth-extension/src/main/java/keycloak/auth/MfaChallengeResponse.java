package keycloak.auth;

import lombok.Data;

import java.util.UUID;

@Data
class MfaChallengeResponse extends MfaResponse{

    public static final String ERR_SERVER_ERROR = "err_server";

    private UUID challengeId;

    private boolean submitted;

    private boolean completed;
}

