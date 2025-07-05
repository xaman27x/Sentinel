package com.moderation.sentinel.util.algorithm.trie;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Trie {
    private final TrieNode root;
    private final Map<String, DetectionResult> cache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> phoneticGroups = new ConcurrentHashMap<>();

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word, String phoneticCode) {
        if (word == null || word.isEmpty()) return;
        
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, ch -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.phoneticCode = phoneticCode;
        current.frequency++;
        
        // This builds the phonetic groups
        if (!phoneticCode.isEmpty()) {
            phoneticGroups.computeIfAbsent(phoneticCode, k -> ConcurrentHashMap.newKeySet()).add(word);
        }
    }

    public DetectionResult contains(String word) {
        if (word == null || word.isEmpty()) {
            return new DetectionResult(false, 0.0, "Empty word");
        }
        
        if (cache.containsKey(word)) {
            return cache.get(word);
        }
        
        TrieNode node = searchNode(word);
        DetectionResult result;
        
        if (node != null && node.isEndOfWord) {
            double confidence = calculateWordConfidence(node, word);
            result = new DetectionResult(true, confidence, "Exact match");
        } else {
            result = new DetectionResult(false, 0.0, "No match");
        }
        
        cache.put(word, result);
        return result;
    }

    public DetectionResult containsPhonetic(String phoneticCode) {
        if (phoneticCode == null || phoneticCode.isEmpty()) {
            return new DetectionResult(false, 0.0, "Empty phonetic code");
        }
        
        Set<String> phoneticMatches = phoneticGroups.get(phoneticCode);
        if (phoneticMatches != null && !phoneticMatches.isEmpty()) {
            // Return the most frequent match
            String bestMatch = phoneticMatches.stream()
                .max((w1, w2) -> Integer.compare(getWordFrequency(w1), getWordFrequency(w2)))
                .orElse(phoneticMatches.iterator().next());
            
            return new DetectionResult(true, 0.85, bestMatch);
        }
        
        return new DetectionResult(false, 0.0, "No phonetic match");
    }
    
    public List<String> findWordsWithPrefix(String prefix, int maxResults) {
        List<String> results = new ArrayList<>();
        TrieNode prefixNode = searchNode(prefix);
        
        if (prefixNode != null) {
            collectWordsWithPrefix(prefixNode, new StringBuilder(prefix), results, maxResults);
        }
        
        return results;
    }
    
    public List<DetectionResult> fuzzySearch(String query, int maxDistance) {
        List<DetectionResult> results = new ArrayList<>();
        fuzzySearchHelper(root, query, "", 0, maxDistance, results);
        
        results.sort((r1, r2) -> Double.compare(r2.confidence, r1.confidence));
        return results.size() > 10 ? results.subList(0, 10) : results;
    }
    
    private void fuzzySearchHelper(TrieNode node, String query, String currentWord, 
                                 int currentDistance, int maxDistance, List<DetectionResult> results) {
        if (currentDistance > maxDistance) return;
        
        if (node.isEndOfWord && currentDistance <= maxDistance) {
            double confidence = 1.0 - (double) currentDistance / Math.max(query.length(), currentWord.length());
            results.add(new DetectionResult(true, confidence, currentWord));
        }
        
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            char c = entry.getKey();
            TrieNode child = entry.getValue();
            String newWord = currentWord + c;
            
            int newDistance = calculateEditDistance(query, newWord);
            
            if (newDistance <= maxDistance) {
                fuzzySearchHelper(child, query, newWord, newDistance, maxDistance, results);
            }
        }
    }
    
    /**
     * Calculates the edit distance between two strings using dynamic programming.
     * This implementation is optimized for performance and can handle larger strings.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return The edit distance between s1 and s2
     */
    private int calculateEditDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        
        return dp[m][n];
    }

    public Set<String> getAllWords() {
        Set<String> words = new HashSet<>();
        collectWords(root, new StringBuilder(), words);
        return words;
    }
    
    private void collectWordsWithPrefix(TrieNode node, StringBuilder word, List<String> results, int maxResults) {
        if (results.size() >= maxResults) return;
        
        if (node.isEndOfWord) {
            results.add(word.toString());
        }
        
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            word.append(entry.getKey());
            collectWordsWithPrefix(entry.getValue(), word, results, maxResults);
            word.deleteCharAt(word.length() - 1);
        }
    }

    private TrieNode searchNode(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private void collectWords(TrieNode node, StringBuilder word, Set<String> words) {
        if (node.isEndOfWord) {
            words.add(word.toString());
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            word.append(entry.getKey());
            collectWords(entry.getValue(), word, words);
            word.deleteCharAt(word.length() - 1);
        }
    }
    
    private double calculateWordConfidence(TrieNode node, String word) {
        double baseConfidence = 0.8;
        double frequencyBonus = Math.min(0.2, node.frequency * 0.01);
        double lengthPenalty = word.length() < 3 ? 0.1 : 0.0;
        
        return Math.min(1.0, baseConfidence + frequencyBonus - lengthPenalty);
    }
    
    private int getWordFrequency(String word) {
        TrieNode node = searchNode(word);
        return node != null ? node.frequency : 0;
    }

    public TrieNode getRoot() {
        return root;
    }

    public static class DetectionResult {
        public final boolean isOffensive;
        public final double confidence;
        public final String message;

        public DetectionResult(boolean isOffensive, double confidence, String message) {
            this.isOffensive = isOffensive;
            this.confidence = confidence;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("Offensive: %b, Confidence: %.2f, Message: %s", isOffensive, confidence, message);
        }
    }
}