package keycloak.auth;

interface MfaClient {

    MfaChallengeResponse requestAuthChallenge(MfaChallengeRequest request);

    MfaVerifyResponse verifyAuthChallenge(MfaVerifyRequest request);
}
