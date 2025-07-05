package com.moderation.sentinel.util.algorithm.trie;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class TrieNode {
    public final Map<Character, TrieNode> children = new ConcurrentHashMap<>();
    public volatile boolean isEndOfWord = false;
    public volatile String phoneticCode = "";
    public volatile int frequency = 0;
    public volatile double severity = 1.0; // 1.0 = normal, 2.0 = high severity
    public volatile long lastAccessed = System.currentTimeMillis();
    
    public final Map<String, Object> metadata = new ConcurrentHashMap<>();
    
    public void incrementFrequency() {
        frequency++;
        lastAccessed = System.currentTimeMillis();
    }
    
    public void setSeverity(double severity) {
        this.severity = Math.max(0.1, Math.min(10.0, severity));
    }
    
    public boolean isHighSeverity() {
        return severity >= 2.0;
    }
}