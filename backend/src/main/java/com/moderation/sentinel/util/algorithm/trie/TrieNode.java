package com.moderation.sentinel.util.algorithm.trie;
import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;
    String phoneticCode; // Soundex Code
}
