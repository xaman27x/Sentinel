package com.moderation.sentinel.util.algorithm.noise;

import java.util.*;
import java.util.regex.Pattern;

public class NoiseDetector {
    private static final Set<Character> COMMON_SEPARATORS = Set.of(
        '.', '-', '_', '*', '~', '`', '^', '|', '\\', '/', '+', '=', 
        '!', '@', '#', '$', '%', '&', '(', ')', '[', ']', '{', '}',
        '<', '>', '?', ':', ';', '"', '\'', ',', ' '
    );
    
    private static final Pattern LEET_SPEAK = Pattern.compile("[0-9@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]");
    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{2,}");
    private static final Pattern UNICODE_VARIATION = Pattern.compile("[\\u0080-\\uFFFF]");
    
    public static String removeObfuscation(String input) {
        if (input == null || input.isEmpty()) return input;
        
        String result = input.toLowerCase();
        
        // Common separators
        result = removeSeparators(result);
        
        // Repeated characters
        result = normalizeRepeatedChars(result);
        
        // Leet speak
        result = convertLeetSpeak(result);
        
        // Unicode variations
        result = normalizeUnicode(result);
        
        // Character insertion patterns
        result = removeCharacterInsertion(result);
        
        return result;
    }
    
    private static String removeSeparators(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (!COMMON_SEPARATORS.contains(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    private static String normalizeRepeatedChars(String input) {
        return REPEATED_CHARS.matcher(input).replaceAll("$1$1");
    }
    
    private static String convertLeetSpeak(String input) {
        Map<String, String> leetMap = Map.of(
            "0", "o", "1", "i", "3", "e", "4", "a", "5", "s",
            "7", "t", "8", "b", "@", "a", "$", "s", "!", "i"
        );
        
        String result = input;
        for (Map.Entry<String, String> entry : leetMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private static String normalizeUnicode(String input) {
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFKD)
            .replaceAll("\\p{M}", "");
    }
    
    // Char Insertion Obfuscation
    private static String removeCharacterInsertion(String input) {
        if (input.length() < 3) return input;
        
        StringBuilder pattern1 = new StringBuilder();
        StringBuilder pattern2 = new StringBuilder();
        
        for (int i = 0; i < input.length(); i++) {
            if (i % 2 == 0) pattern1.append(input.charAt(i));
            else pattern2.append(input.charAt(i));
        }
        
        return pattern1.length() > pattern2.length() ? pattern1.toString() : input;
    }
    
    // Confidence Score
    public static double calculateObfuscationScore(String original, String deobfuscated) {
        if (original.equals(deobfuscated)) return 0.0;
        
        double lengthRatio = (double) deobfuscated.length() / original.length();
        double separatorCount = countSeparators(original);
        double repeatCount = countRepeatedChars(original);
        
        return Math.min(1.0, (separatorCount + repeatCount + (1 - lengthRatio)) / 3);
    }
    
    private static double countSeparators(String input) {
        long count = input.chars().mapToObj(c -> (char) c)
            .filter(COMMON_SEPARATORS::contains).count();
        return Math.min(1.0, (double) count / input.length());
    }
    
    private static double countRepeatedChars(String input) {
        int repeatedCount = 0;
        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == input.charAt(i - 1)) {
                repeatedCount++;
            }
        }
        return Math.min(1.0, (double) repeatedCount / input.length());
    }
    
    // Obfuscation Variants
    public static Set<String> generateObfuscationVariants(String word) {
        Set<String> variants = new HashSet<>();
        variants.add(word);
        
        for (char sep : List.of('.', '-', '*', '_')) {
            variants.add(insertSeparators(word, sep));
        }
        
        variants.add(addRandomRepeats(word));
        
        variants.add(toLeetSpeak(word));
        
        return variants;
    }
    
    private static String insertSeparators(String word, char separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            result.append(word.charAt(i));
            if (i < word.length() - 1) result.append(separator);
        }
        return result.toString();
    }
    
    private static String addRandomRepeats(String word) {
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (char c : word.toCharArray()) {
            result.append(c);
            if (random.nextBoolean()) result.append(c);
        }
        return result.toString();
    }
    
    private static String toLeetSpeak(String word) {
        return word.replace('a', '@')
                  .replace('e', '3')
                  .replace('i', '1')
                  .replace('o', '0')
                  .replace('s', '$')
                  .replace('t', '7');
    }
}