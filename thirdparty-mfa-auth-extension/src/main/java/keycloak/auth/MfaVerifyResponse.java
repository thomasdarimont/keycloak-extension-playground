package keycloak.auth;

import lombok.Data;

@Data
class MfaVerifyResponse extends MfaResponse {

    public static final String ERR_INVALID_CODE = "err_invalid_code";

    public static final String ERR_TIMEOUT = "err_timeout";

    public static final String ERR_MALFORMED_INPUT = "err_malformed_input";

    private boolean successful;

    private boolean completed;
}

