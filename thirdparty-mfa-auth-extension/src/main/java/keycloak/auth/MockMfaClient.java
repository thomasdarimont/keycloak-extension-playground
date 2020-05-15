package keycloak.auth;

import lombok.extern.jbosslog.JBossLog;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@JBossLog
public class MockMfaClient implements MfaClient {

    private static final Map<String, Instant> CHALLENGES = new HashMap<>();
    private static final Map<String, String> OTP_CODES = new HashMap<>();
    private static final Map<String, AtomicInteger> OTP_FAIL_COUNTER = new HashMap<>();
    private static final Map<String, String> USERS = new HashMap<>();

    @Override
    public MfaChallengeResponse requestAuthChallenge(MfaChallengeRequest request) {

        String username = request.getUsername();

        MfaChallengeResponse challenge = new MfaChallengeResponse();

        if ("servererror".equals(username)) {
            challenge.setCompleted(false);
            challenge.setSubmitted(false);
            challenge.setErrorCode(MfaChallengeResponse.ERR_SERVER_ERROR);
            return challenge;
        }

        challenge.setChallengeId(UUID.randomUUID());
        challenge.setErrorCode(null);

        UUID challengeId = challenge.getChallengeId();
        String challengeIdString = challengeId.toString();
        CHALLENGES.put(challengeIdString, Instant.now());
        USERS.put(challengeIdString, username);

        switch (request.getMfaMethod()) {
            case OTP:
                String otpCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
                OTP_CODES.put(challengeIdString, otpCode);
                OTP_FAIL_COUNTER.put(challengeIdString, new AtomicInteger());
                log.infof("Generated OTP code for challenge. otp=%s challengeId=%s", otpCode, challengeIdString);
                break;
        }
        challenge.setSubmitted(true);

        if ("probe".equals(username)) {
            challenge.setCompleted(true);
        }

        return challenge;
    }

    @Override
    public MfaVerifyResponse verifyAuthChallenge(MfaVerifyRequest request) {

        String challengeIdString = request.getChallengeId().toString();
        Instant instant = CHALLENGES.get(challengeIdString);


        if (instant.plus(1, ChronoUnit.MINUTES).isBefore(Instant.now())) {
            MfaVerifyResponse response = new MfaVerifyResponse();
            response.setSuccessful(false);
            response.setCompleted(true);
            response.setErrorCode(MfaVerifyResponse.ERR_TIMEOUT);
            return response;
        }

        if (!OTP_CODES.containsKey(challengeIdString)) {
            MfaVerifyResponse response = new MfaVerifyResponse();
            boolean fakePushReceived = instant.plusSeconds(7).isBefore(Instant.now()) && !"timeout".equals(USERS.get(challengeIdString));
            response.setSuccessful(fakePushReceived);
            response.setCompleted(fakePushReceived);
            response.setErrorCode(null);
            return response;
        }

        String otp = OTP_CODES.get(challengeIdString);

        String challengeInput = request.getChallengeInput();
        if (challengeInput == null || challengeInput.isEmpty() || !challengeInput.matches("\\d+")) {
            MfaVerifyResponse response = new MfaVerifyResponse();
            response.setSuccessful(false);
            response.setCompleted(isOtpAttemptsExceeded(request));
            response.setErrorCode(MfaVerifyResponse.ERR_MALFORMED_INPUT);
            return response;
        }


        MfaVerifyResponse response = new MfaVerifyResponse();
        boolean successful = challengeInput.equals(otp);
        response.setSuccessful(successful);
        response.setCompleted(successful || isOtpAttemptsExceeded(request));
        response.setErrorCode(successful ? null : MfaVerifyResponse.ERR_INVALID_CODE);
        return response;

    }

    private boolean isOtpAttemptsExceeded(MfaVerifyRequest request) {
        return OTP_FAIL_COUNTER.get(request.getChallengeId().toString()).incrementAndGet() > 3;
    }
}
