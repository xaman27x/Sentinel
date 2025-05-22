package com.moderation.sentinel.util.algorithm.trie;

public class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, ch -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    public boolean contains(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            if (!current.children.containsKey(c)) return false;
            current = current.children.get(c);
        }
        return current.isEndOfWord;
    }

    public TrieNode getRoot() {
        return root;
    }
}
