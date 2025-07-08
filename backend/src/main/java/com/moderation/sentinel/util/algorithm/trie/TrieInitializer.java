package com.moderation.sentinel.util.algorithm.trie;

import com.moderation.sentinel.util.algorithm.normalization.TextNormalizer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class TrieInitializer {
    private static final Logger logger = Logger.getLogger(TrieInitializer.class.getName());

    @Value("${application.aes-secret-key}")
    private String secretKey;

    @Value("${application.init-vector}")
    private String initVector;

    private final Trie offensiveTrie = new Trie();
    private final Trie safeTrie = new Trie();

    public Trie getOffensiveTrie() {
        return offensiveTrie;
    }

    public Trie getSafeTrie() {
        return safeTrie;
    }

    @PostConstruct
    public void init() throws Exception {
        Map<String, Trie> tries = initializeFromClasspath("offensive_words.dat");
        offensiveTrie.getRoot().children.putAll(tries.get("offensive").getRoot().children);
        safeTrie.getRoot().children.putAll(tries.get("safe").getRoot().children);
    }

    public Map<String, Trie> initializeFromClasspath(String resourceName) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new FileNotFoundException(resourceName + " not found in classpath");
            }

            byte[] encryptedBytes = inputStream.readAllBytes();

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            String decryptedContent = new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
            List<String> words = List.of(decryptedContent.split("\\R"));

            Map<String, Trie> tries = new HashMap<>();

            Trie offensiveTrie = new Trie();
            int wordCount = 0;
            for (String word : words) {
                String trimmedWord = word.trim().toLowerCase();
                if (!trimmedWord.isEmpty() && !trimmedWord.startsWith("#")) {
                    String phoneticCode = computeSoundex(trimmedWord);
                    offensiveTrie.insert(trimmedWord, phoneticCode);
                    wordCount++;
                }
            }

            logger.info("Loaded " + wordCount + " terms into content filter");
            tries.put("offensive", offensiveTrie);

            Trie safeTrie = new Trie();
            tries.put("safe", safeTrie);

            return tries;
        }
    }


    public void addTermToEncryptedFile(String encryptedFilePath, String term) throws Exception {
        Path path = Path.of(encryptedFilePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Encrypted file does not exist");
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] encryptedBytes = Files.readAllBytes(path);
        String decryptedContent = new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);

        String trimmedTerm = term.trim().toLowerCase();
        if (decryptedContent.contains(trimmedTerm)) {
            logger.info("Term already exists in database");
            return;
        }


        String updatedContent = decryptedContent + System.lineSeparator() + trimmedTerm;
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] updatedEncryptedBytes = cipher.doFinal(updatedContent.getBytes(StandardCharsets.UTF_8));
        Files.write(path, updatedEncryptedBytes);

        String phoneticCode = computeSoundex(trimmedTerm);
        offensiveTrie.insert(trimmedTerm, phoneticCode);
        
    }

    public static String computeSoundex(String input) {
        if (input == null || input.isEmpty()) return "";

        StringBuilder code = new StringBuilder();
        input = input.toLowerCase().replaceAll("[^a-z]", "");
        if (input.isEmpty()) return "";

        code.append(Character.toUpperCase(input.charAt(0)));

        Map<Character, Character> soundexMap = getSoundexMappings();

        char prevCode = '0';
        for (int i = 1; i < input.length() && code.length() < 4; i++) {
            char c = input.charAt(i);
            if ("aeiouhwy".indexOf(c) == -1) {
                char digit = soundexMap.getOrDefault(c, '0');
                if (digit != '0' && digit != prevCode) {
                    code.append(digit);
                    prevCode = digit;
                }
            }
        }

        // Padding with zeroes
        while (code.length() < 4) {
            code.append('0');
        }
        
        return code.length() > 4 ? code.substring(0, 4) : code.toString();
    }

    private static Map<Character, Character> getSoundexMappings() {
        Map<Character, Character> soundexMap = new HashMap<>();
        // Soundex mapping groups
        soundexMap.put('b', '1'); soundexMap.put('f', '1'); soundexMap.put('p', '1'); soundexMap.put('v', '1');
        soundexMap.put('c', '2'); soundexMap.put('g', '2'); soundexMap.put('j', '2'); soundexMap.put('k', '2');
        soundexMap.put('q', '2'); soundexMap.put('s', '2'); soundexMap.put('x', '2'); soundexMap.put('z', '2');
        soundexMap.put('d', '3'); soundexMap.put('t', '3');
        soundexMap.put('l', '4');
        soundexMap.put('m', '5'); soundexMap.put('n', '5');
        soundexMap.put('r', '6');
        return soundexMap;
    }
}