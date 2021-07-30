package com.github.thomasdarimont.keycloak.auth.smstwilio;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import java.util.HashMap;
import java.util.Map;

@JBossLog
public class TwilioApi {

    public static final String DEFAULT_API_URI = "https://api.authy.com";
    public static final String PHONE_VERIFICATION_API_PATH = "/protected/json/phones/verification/";
    public static final String X_AUTHY_API_KEY = "X-Authy-API-Key";

    private final KeycloakSession session;
    private final String apiKey;
    private final String codeLen;

    public TwilioApi(KeycloakSession session, String apiKey, String codeLen) {
        this.session = session;
        this.apiKey = apiKey;
        this.codeLen = codeLen;
    }

    public boolean sendSms(String phoneNumber) {

        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", phoneNumber);
        data.put("country_code", "49"); // Germany
        data.put("via", "sms"); // SMS
        data.put("code_length", codeLen);

        SimpleHttp http = SimpleHttp.doPost(DEFAULT_API_URI + PHONE_VERIFICATION_API_PATH + "start", session);
        http.header(X_AUTHY_API_KEY, apiKey); // API-KEY
        http.json(data);
        try {
            SimpleHttp.Response response = http.asResponse();
            if (response.getStatus() == 200) {
                return true;
            }
        } catch (Exception ex) {
            log.warnf(ex, "Could not send sms.");
        }

        return false;
    }

    public boolean verifySmsCode(String phoneNumber, String code) {

        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", phoneNumber);
        data.put("country_code", "49"); // Germany
        data.put("verification_code", code);
        data.put("code_length", codeLen);

        SimpleHttp http = SimpleHttp.doGet(DEFAULT_API_URI + PHONE_VERIFICATION_API_PATH + "check", session);
        http.header(X_AUTHY_API_KEY, apiKey); // API-KEY
        for (String key : data.keySet()) {
            http.param(key, String.valueOf(data.get(key)));
        }
        try {
            SimpleHttp.Response response = http.asResponse();
            if (response.getStatus() == 200) {
                return true;
            }
        } catch (Exception ex) {
            log.warnf(ex, "Could not verify code.");
        }

        return false;
    }
}