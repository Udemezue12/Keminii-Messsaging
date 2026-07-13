package com.astrotech.chat.core;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class GetSecretKey {
    public static SecretKey getKeys(String secretKey) {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }
    public static SecretKeySpec getKeysSpec(byte[] key, String algorithm){
        return new SecretKeySpec(key, algorithm);
    }

}
