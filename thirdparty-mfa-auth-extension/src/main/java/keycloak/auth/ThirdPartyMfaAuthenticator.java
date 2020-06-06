package keycloak.auth;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.beans.MessageFormatterMethod;
import org.owasp.html.Sanitizers;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@JBossLog
public class ThirdPartyMfaAuthenticator implements Authenticator {

    public static final String MFA_METHOD = "mfa_method";
    public static final String USE_OTP = "useOtp";
    public static final String MFA_CHALLENGE = "mfaChallenge";
    public static final String MFA_CHALLENGE_START = "mfaChallengeStart";

    public static final String MFA_SESSION_MARKER_KEY = "mfa";

    private final MfaClient mfaClient;
    private final KeycloakSession session;

    public ThirdPartyMfaAuthenticator(KeycloakSession session, MfaClient mfaClient) {
        this.session = session;
        this.mfaClient = mfaClient;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        String username = user.getUsername();

        log.infof("Request MFA for User. username=%s", username);

        String existingMfaSessionMarker = session.sessions().getUserSessions(realm, user).stream()
                // TODO ensure user comes from the same device
                .filter(us -> us.getNote(MFA_SESSION_MARKER_KEY) != null)
                .map(us -> us.getNote(MFA_SESSION_MARKER_KEY))
                .findFirst()
                .orElse(null);

        if (existingMfaSessionMarker != null) {
            // There is already an existing user session that was authenticated via MFA

            // TODO check max time since last mfa validation
            String[] items = existingMfaSessionMarker.split(";");
            long mfaAuthTime = Long.parseLong(items[0]);
            MfaMethod mfaMethod = MfaMethod.valueOf(items[1]);

            log.infof("MFA already valid for this session, skipping mfa check. realm=%s username=%s mfa_method=%s mfa_challenge_timestamp=%s",
                    realm.getName(), username, mfaMethod, mfaAuthTime);
            context.success();
            return;
        }

        requestMfaChallenge(context, username, context.getAuthenticationSession());
    }

    private Duration computeChallengeDuration(AuthenticationSessionModel authSession) {

        long mfaChallengeStart = Long.parseLong(authSession.getAuthNote(MFA_CHALLENGE_START));
        return Duration.between(Instant.ofEpochMilli(mfaChallengeStart), Instant.now());
    }

    private Response createChallengeFormResponse(AuthenticationFlowContext context, boolean firstTry, MfaMethod mfaMethod, MfaResponse mfaResponse) {

        LoginFormsProvider form = context.form()
                .setAttribute(MFA_METHOD, mfaMethod.name())
                .setAttribute("mfa_error", mfaResponse.getErrorCode());

        if (MfaMethod.PUSH.equals(mfaMethod)) {
            form.setAttribute("hint", firstTry ? "mfa_push_await_challenge_response" : "mfa_push_await_challenge_response");
        }

        Locale locale = session.getContext().resolveLocale(context.getUser());
        form.setAttribute("customMsg", new MessageFormatterMethod(locale, MfaMessages.getMessages()));

        if (mfaResponse.getErrorCode() != null) {
            if (MfaVerifyResponse.ERR_INVALID_CODE.equals(mfaResponse.getErrorCode())) {
                form.setError(Messages.INVALID_TOTP);
            } else {
                form.setError(mfaResponse.getErrorCode());
            }
        }

        switch (mfaMethod) {
            case OTP:
                return form.createForm("custom-mfa-form-otp.ftl");

            case PUSH:
            default:
                return form.createForm("custom-mfa-form-push.ftl");
        }

    }

    private MfaChallengeRequest createMfaChallengeRequest(String username, AuthenticationSessionModel authSession) {

        MfaChallengeRequest mfaRequest = new MfaChallengeRequest();
        mfaRequest.setUsername(username);
        String mfaMethod = authSession.getAuthNote(MFA_METHOD);
        if (MfaMethod.OTP.name().equals(mfaMethod)) {
            mfaRequest.setMfaMethod(MfaMethod.OTP);
        }

        return mfaRequest;
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("cancel")) {
            context.resetFlow();
            context.fork();
            return;
        }

        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        String username = user.getUsername();
        log.infof("Request MFA for User. username=%s", username);

        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        MfaMethod mfaMethod = MfaMethod.resolve(authSession.getAuthNote(MFA_METHOD));

        if (formData.containsKey(USE_OTP)) {
            authSession.setAuthNote(MFA_METHOD, MfaMethod.OTP.name());
            requestMfaChallenge(context, username, authSession);
            return;
        }

        String mfaChallengeId = authSession.getAuthNote(MFA_CHALLENGE);
        log.infof("Found challengeId=%s", mfaChallengeId);

        MfaVerifyRequest mfaRequest = new MfaVerifyRequest();
        mfaRequest.setChallengeId(UUID.fromString(mfaChallengeId));
        mfaRequest.setChallengeInput(Sanitizers.BLOCKS.sanitize(formData.getFirst("challenge_input")));

        MfaVerifyResponse mfaVerifyResponse = mfaClient.verifyAuthChallenge(mfaRequest);

        if (mfaVerifyResponse.isSuccessful()) {

            log.infof("MFA authentication successful. realm=%s username=%s mfa_method=%s mfa_challenge_duration=%s", realm.getName(), username, mfaMethod, computeChallengeDuration(authSession));

            signalSuccessfulMfaAuthentication(context, authSession, mfaMethod);
            return;
        }

        if (mfaVerifyResponse.isCompleted()) {
            log.infof("MFA authentication failed. realm=%s username=%s error_code=%s mfa_method=%s mfa_challenge_duration=%s", realm.getName(), user.getUsername(), mfaVerifyResponse.getErrorCode(), mfaMethod, computeChallengeDuration(authSession));
            context.getEvent().user(user);

            String errorMessage = Messages.LOGIN_TIMEOUT;
            if (MfaVerifyResponse.ERR_TIMEOUT.equals(mfaVerifyResponse.getErrorCode())) {
                context.getEvent().error(Errors.SESSION_EXPIRED);
            } else {
                errorMessage = Messages.INVALID_TOTP;
                context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            }
            context.resetFlow();
            context.forkWithErrorMessage(new FormMessage(errorMessage));
            return;
        }

        log.infof("MFA authentication attempt failed. Retrying realm=%s username=%s error_code=%s mfa_method=%s", realm.getName(), user.getUsername(), mfaVerifyResponse.getErrorCode(), mfaMethod);

        Response response = createChallengeFormResponse(context, false, mfaMethod, mfaVerifyResponse);

        context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, response);
    }

    private void signalSuccessfulMfaAuthentication(AuthenticationFlowContext context, AuthenticationSessionModel authSession, MfaMethod mfaMethod) {

        authSession.removeAuthNote(MFA_CHALLENGE);
        authSession.removeAuthNote(MFA_CHALLENGE_START);

        authSession.setUserSessionNote(MFA_SESSION_MARKER_KEY, System.currentTimeMillis() + ";" + mfaMethod);
        context.success();
    }

    private void requestMfaChallenge(AuthenticationFlowContext context, String username, AuthenticationSessionModel authSession) {

        MfaChallengeRequest mfaRequest = createMfaChallengeRequest(username, authSession);
        MfaChallengeResponse mfaResponse = mfaClient.requestAuthChallenge(mfaRequest);

        MfaMethod mfaMethod = mfaRequest.getMfaMethod();
        if (mfaResponse.isCompleted()) {
            log.infof("MFA Challenge immediately completed. username=%s challengeId=%s mfa_method=%s mfa_challenge_duration=%s", username, mfaResponse.getChallengeId(), mfaMethod, computeChallengeDuration(authSession));

            signalSuccessfulMfaAuthentication(context, authSession, mfaMethod);
            return;
        }

        if (mfaResponse.isSubmitted()) {
            log.infof("Retrieved challengeId=%s", mfaResponse.getChallengeId());
            authSession.setAuthNote(MFA_CHALLENGE, mfaResponse.getChallengeId().toString());
            authSession.setAuthNote(MFA_CHALLENGE_START, String.valueOf(System.currentTimeMillis()));

            Response response = createChallengeFormResponse(context, true, mfaRequest.getMfaMethod(), mfaResponse);
            context.challenge(response);
            return;
        }

        log.warnf("MFA Challenge request failed. username=%s challengeId=%s mfa_error=%s", username, mfaResponse.getChallengeId(), mfaResponse.getErrorCode());
        context.forkWithErrorMessage(new FormMessage(Messages.FAILED_TO_PROCESS_RESPONSE));
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
