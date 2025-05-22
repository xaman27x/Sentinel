package com.moderation.sentinel.util.algorithm.trie;

import org.springframework.stereotype.Controller;

public class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode current = root;
        for(char c: word.toCharArray()) {
            current = current.children.computeIfAbsent(c, x -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    public boolean contains(String word) {
        TrieNode current = root;
        for( char c: word.toCharArray()) {
            if(!current.children.containsKey(c)) return false;
            current = current.children.get(c);
        }
        return current.isEndOfWord;
    }
}
