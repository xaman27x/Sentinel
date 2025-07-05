package com.moderation.sentinel.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @Value("${application.encryption.secret-key}")
    private String secretKeyString;
    
    public String encrypt(String plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(secretKeyString.getBytes(), ALGORITHM);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        
        byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
        
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }
    
    public String decrypt(String encryptedText) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedText);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, iv.length);
        
        byte[] encryptedData = new byte[decodedData.length - GCM_IV_LENGTH];
        System.arraycopy(decodedData, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
        
        SecretKeySpec secretKey = new SecretKeySpec(secretKeyString.getBytes(), ALGORITHM);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        
        byte[] decryptedData = cipher.doFinal(encryptedData);
        
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
    
    public static String generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
    
    public String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "sk-" + Base64.getEncoder().encodeToString(bytes)
                .replace("+", "")
                .replace("/", "")
                .replace("=", "")
                .substring(0, 40);
    }
}