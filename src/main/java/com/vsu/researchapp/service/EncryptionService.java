package com.vsu.researchapp.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int ITERATIONS = 65536;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int IV_LENGTH_BYTES = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public byte[] generateRandomSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public byte[] generateRandomIv() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);
        return iv;
    }

    public byte[] encrypt(byte[] plaintext, String passphrase, byte[] salt, byte[] iv) throws Exception {
        SecretKeySpec keySpec = deriveKey(passphrase, salt);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext, String passphrase, byte[] salt, byte[] iv) throws Exception {
        SecretKeySpec keySpec = deriveKey(passphrase, salt);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(ciphertext);
    }

    private SecretKeySpec deriveKey(String passphrase, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
