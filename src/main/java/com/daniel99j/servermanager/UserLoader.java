package com.daniel99j.servermanager;

import com.daniel99j.servermanager.site.SiteUtil;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UserLoader {
    private static final List<Instant> failedAttempts = new ArrayList<>();

    public static boolean login(HttpExchange exchange) {
        return login(getCookieValue(exchange.getRequestHeaders().getFirst("Cookie"), "Token"));
    }

    public static boolean login(String password) {
        failedAttempts.removeIf(instant -> instant.isBefore(Instant.now().minusSeconds(60)));
        if(failedAttempts.size() > 5) return false;
        boolean worked = hashPassword(password).equals(Config.INSTANCE.loginKey);
        if(!worked) failedAttempts.add(Instant.now());
        return worked;
    }

    public static boolean checkLoggedIn(HttpExchange exchange) {
        if (login(exchange)) {
            return true;
        } else {
            SiteUtil.respond(exchange, "Not Authorised", 401);
            return false;
        }
    }

    //no extra to use here as theres only 1 user!
    public static String hashPassword(String password) {
        String combined = password;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            //The HASHED: tells me that the config is for a token not password
            return "HASHED: "+hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCookieValue(String cookieHeader, String key) {
        if (cookieHeader == null) return "";

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] pair = cookie.trim().split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1].replace("\"", "");
            }
        }
        return "";
    }
}
