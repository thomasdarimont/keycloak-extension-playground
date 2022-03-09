package com.github.thomasdarimont.keycloak.trustdevice.support;

import lombok.extern.jbosslog.JBossLog;
import ua_parser.Parser;
import ua_parser.UserAgent;

import java.io.IOException;

@JBossLog
public class UserAgentParser {

    private static final Parser USER_AGENT_PARSER;

    static {
        Parser parser = null;
        try {
            parser = new Parser();
        } catch (RuntimeException e) {
            log.errorf(e, "Could not initialize user_agent parser");
        }
        USER_AGENT_PARSER = parser;
    }

    public static UserAgent parseUserAgent(String userAgentString) {

        if (USER_AGENT_PARSER == null) {
            return null;
        }

        return USER_AGENT_PARSER.parseUserAgent(userAgentString);
    }
}
