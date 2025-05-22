package com.moderation.sentinel.util.algorithm.trie;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Algorithm Imports
import com.moderation.sentinel.util.algorithm.normalization.*;

public class TrieInitializer {
    private static final String SECRET_KEY = "SymmetricEncryptionKey";
    private static final String INIT_VECTOR = "RandomInitVector";

    public static Trie initialize(String encryptedFilePath) throws Exception {
        byte[] encryptedBytes = Files.readAllBytes(Path.of(encryptedFilePath));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        String decryptedContent = new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
        List<String> words = List.of(decryptedContent.split("\\R"));

        Trie trie= new Trie();
        for(String word: words) {
            trie.insert(word.trim().toLowerCase());
        }
        return trie;
    }

    // Test function for trie
    public static void main(String[] args) throws Exception {
        Trie trie = TrieInitializer.initialize("backend/offensive_words.dat");
        String normalizedInput = TextNormalizer.normalize("Enter the test word here");
        System.out.println(trie.contains(normalizedInput));
        System.out.println(trie.contains("notbadword"));
    }
}
