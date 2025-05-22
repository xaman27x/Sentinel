package com.moderation.sentinel.util.algorithm.trie;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word, String phoneticCode) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, ch -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.phoneticCode = phoneticCode;
    }

    public DetectionResult contains(String word) {
        TrieNode node = searchNode(word);

        if(node != null && node.isEndOfWord) {
            return new DetectionResult(true, 1.0, "Offensive");
        } else {
            return new DetectionResult(false, 0.0, "Safe");
        }
    }

    public Set<String> getAllWords() {
        Set<String> words = new HashSet<>();
        collectWords(root, new StringBuilder(), words);
        return words;
    }

    public DetectionResult containsPhonetic(String phoneticCode) {
        return searchPhonetic(root, phoneticCode, new StringBuilder());
    }

    private TrieNode searchNode(String word) {
        TrieNode current = root;
        for(char c: word.toCharArray()) {
            current = current.children.get(c);
            if(current == null) {
                return null;
            }
        }
        return current;
    }

    private DetectionResult searchPhonetic(TrieNode node, String phoneticCode, StringBuilder word) {
        if(node.isEndOfWord && node.phoneticCode != null && node.phoneticCode.equals(phoneticCode)) {
            return new DetectionResult(true, 1.0, word.toString());
        }

        for(Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            word.append(entry.getKey());
            DetectionResult result = searchPhonetic(entry.getValue(), phoneticCode, word);

            if(result.isOffensive) {
                return result;
            }
            word.deleteCharAt(word.length() - 1);
        }
        return new DetectionResult(false, 0.0, "No Match");
    }

    private void collectWords(TrieNode node, StringBuilder word, Set<String> words) {
        if(node.isEndOfWord) {
            words.add(word.toString());
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            word.append(entry.getKey());
            collectWords(entry.getValue(), word, words);
            word.deleteCharAt(word.length() - 1);
        }
    }

    public TrieNode getRoot() {
        return root;
    }

    /*
        Result will be returned as a DetectionResult class
     */
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
