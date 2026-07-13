package com.astrotech.chat.util;

import com.astrotech.chat.configProperties.EncryptionProperties;
import com.astrotech.chat.core.GetSecretKey;
import com.astrotech.chat.dto.response.EncryptedData;
import com.astrotech.chat.entites.Message;
import com.astrotech.chat.exceptions.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@RequiredArgsConstructor
@Component
@Slf4j
public class EncryptionUtil {
    private final EncryptionProperties properties;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }



    @PostConstruct
    public void init() {
        var keyBytes = hexToBytes(properties.getServerKeyHex().substring(0, 64));
        var server = GetSecretKey.getKeysSpec(keyBytes, "AES");
        log.info("Encryption service initialized with AES-256-GCM");
    }

    public EncryptedData encrypt(String plaintext) {
        try {
            var iv = generateIv();
            var secretKey = getSecretKey();
            var cipher = Cipher.getInstance(properties.getAesAlgorithm(), "BC");
            var spec = new GCMParameterSpec(properties.getGcmTagLength(), iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            var cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedData(
                    Base64.getEncoder().encodeToString(cipherText),
                    Base64.getEncoder().encodeToString(iv)
            );

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException("Encryption failed");

        }
    }


    public String decrypt(String ciphertext, String ivBase64) {
        try {
            var iv = Base64.getDecoder().decode(ivBase64);
            var data = Base64.getDecoder().decode(ciphertext);
            var secretKey = getSecretKey();
            var cipher = Cipher.getInstance(properties.getAesAlgorithm(), "BC");
            var spec = new GCMParameterSpec(properties.getGcmTagLength(), iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException("Decryption failed");
        }
    }

    public EncryptedData encryptWithKey(String plaintext, String keyBase64) {
        try {
            var keyBytes = Base64.getDecoder().decode(keyBase64);
            var key = GetSecretKey.getKeysSpec(keyBytes, "AES");
            byte[] iv = generateIv();
            var cipher = Cipher.getInstance(properties.getAesAlgorithm(), "BC");
            cipher.init(
                    Cipher.ENCRYPT_MODE, key,
                    new GCMParameterSpec(
                            properties.getGcmTagLength(),
                            iv));
            var ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedData(
                    Base64.getEncoder().encodeToString(ciphertext),
                    Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException("Ephemeral encryption failed");
        }
    }

    public KeyPair generateRsaKeyPair() {
        try {
            var gen = KeyPairGenerator.getInstance("RSA", "BC");
            gen.initialize(2048, new SecureRandom());
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("RSA key generation failed", e);
        }
    }

    public String encryptWithPublicKey(String data, String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            PublicKey publicKey = KeyFactory.getInstance("RSA", "BC")
                    .generatePublic(new X509EncodedKeySpec(keyBytes));
            Cipher cipher = Cipher.getInstance(properties.getRsaAlgorithm(), "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException("RSA encryption failed");
        }
    }
    public String decryptContent(String content, String contentIv) {
        if (content == null || contentIv == null) return null;
        try {
            return decrypt(content, contentIv);
        } catch (Exception e) {
            return "[encrypted]";
        }
    }


    public String sha256Checksum(byte[] data) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return bytesToHex(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new BadRequestException("CheckSum Failed");
        }
    }
    public String sha256Checksum(InputStream inputStream) {

        try {

            var digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hash = digest.digest();

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException("Failed to calculate SHA-256 checksum");
        }
    }

    public String generateSecureToken(int bytes) {
        byte[] token = new byte[bytes];
        new SecureRandom().nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    public String generateAesKeyBase64() {
        try {
            var gen = KeyGenerator.getInstance("AES");
            gen.init(256, new SecureRandom());
            return Base64.getEncoder().encodeToString(gen.generateKey().getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] hexToBytes(String hex) {
        var len = hex.length();
        var data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private @NonNull SecretKey getSecretKey() {
    try {
        // Deterministically hash whatever string length you have into exactly 32 bytes
        var digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(properties.getServerSecret().getBytes(StandardCharsets.UTF_8));
        
        // Build the AES secret key spec out of the 32-byte hash
        return GetSecretKey.getKeysSpec(keyBytes, "AES");
    } catch (Exception e) {
        log.error("Failed to derive secure AES key from server secret", e);
        throw new BadRequestException("Encryption setup failed");
    }
}
    private byte[] generateIv() {
        var iv = new byte[properties.getIvLength()];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private String bytesToHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
