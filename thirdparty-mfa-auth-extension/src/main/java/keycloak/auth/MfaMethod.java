package keycloak.auth;

enum MfaMethod {
    PUSH, OTP;


    public static MfaMethod resolve(String name) {

        if (name == null) {
            return MfaMethod.PUSH;
        }

        return MfaMethod.valueOf(name);
    }
}
