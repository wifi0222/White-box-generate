package com.example.util;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class SessionScopedUtil {
    public static Map<String, Object> getScopedSession(HttpSession session, String token) {
        String key = "sessionToken_" + token;
        Map<String, Object> scoped = (Map<String, Object>) session.getAttribute(key);
        if (scoped == null) {
            scoped = new HashMap<>();
            session.setAttribute(key, scoped);
        }
        return scoped;
    }
}

