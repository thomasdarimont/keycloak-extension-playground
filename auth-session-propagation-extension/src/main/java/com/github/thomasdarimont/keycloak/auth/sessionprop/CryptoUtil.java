package com.github.thomasdarimont.keycloak.auth.sessionprop;

import org.apache.commons.codec.binary.Base64;
import org.keycloak.common.util.RandomString;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class CryptoUtil {

    public static final String ALG = "AES";
    public static final String ALG_TRANSFORMATION = "AES/CBC/PKCS5PADDING";

    public static void main(String[] args) {

        long timestamp = 1589807024377L;
        String username = "tester";
        String sessionHandle = UUID.randomUUID().toString();

        String payload = timestamp + ";" + username + ";" + sessionHandle;

        System.out.println(payload);
        String key = RandomString.randomCode(64);
        String salt = RandomString.randomCode(64);

        String encrypted = encrypt(payload, key + salt);
        System.out.println(encrypted);

        String decrypted = decrypt(encrypted, key + salt);
        System.out.println(decrypted);

    }

    public static String encrypt(String payload, String key) {

        try {
            byte[] keyBytes = sha256(key);
            byte[] ivBytes = Arrays.copyOf(keyBytes, 16);

            Cipher cipher = Cipher.getInstance(ALG_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, ALG), new IvParameterSpec(ivBytes));

            byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64URLSafeString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static String decrypt(String encrypted, String key) {
        try {

            byte[] keyBytes = sha256(key);
            byte[] ivBytes = Arrays.copyOf(keyBytes, 16);

            Cipher cipher = Cipher.getInstance(ALG_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, ALG), new IvParameterSpec(ivBytes));

            return new String(cipher.doFinal(Base64.decodeBase64(encrypted)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static byte[] sha256(String key) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        md.digest(keyBytes);

        return md.digest();
    }

}
