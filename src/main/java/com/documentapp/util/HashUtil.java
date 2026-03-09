package com.documentapp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {

    public static String sha256(String input) {

        if (input == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) {
                    hex.append('0');
                }
                hex.append(s);
            }

            return hex.toString();   

        } catch (Exception e) {
            throw new RuntimeException("Error while hashing", e);
        }
    }

    
    public static String hashEmail(String email) {

        if (email == null) {
            return null;
        }

       
        String normalized = email.trim().toLowerCase();

        return sha256(normalized);
    }
}