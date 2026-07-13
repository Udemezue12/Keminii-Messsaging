package com.astrotech.chat.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class AppGenerators {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE_64_ENCODER = Base64.getUrlEncoder().withoutPadding();

    public static String generateUniqueSessionId() {

        var uuid = generateTimestampedUUID();


        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        String secureRandomString = BASE_64_ENCODER.encodeToString(randomBytes);

        
        return (uuid + "-" + secureRandomString).replaceAll("[^a-zA-Z0-9]", "");
    }
    public static String generateTimestampedUUID() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
    }
    public static String hashToken(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static String generateSessionKey() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
