package com.documentapp.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class AuthUtil {

    public static Long getUserIdFromCookie(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("userId".equals(cookie.getName())) {
                try {
                    
                    return Long.parseLong(cookie.getValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }
}