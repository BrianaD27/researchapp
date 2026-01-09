package com.vsu.researchapp.infrastructure.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class AesEncryptor {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12;   // bytes
    private static final SecureRandom RNG = new SecureRandom();

    private AesEncryptor() {}

    private static SecretKeySpec key() {
        String keyStr = System.getenv("FIELD_ENC_KEY");
        if (keyStr == null || keyStr.isBlank()) {
            throw new IllegalStateException("Missing env var FIELD_ENC_KEY (must be 16/24/32 chars for AES).");
        }
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        int len = keyBytes.length;
        if (len != 16 && len != 24 && len != 32) {
            throw new IllegalStateException("FIELD_ENC_KEY must be 16, 24, or 32 characters (AES-128/192/256).");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            RNG.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);

            if (combined.length <= IV_LENGTH) {
                throw new IllegalStateException("Ciphertext too short");
            }

            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed (wrong key or corrupted data)", e);
        }
    }
}
