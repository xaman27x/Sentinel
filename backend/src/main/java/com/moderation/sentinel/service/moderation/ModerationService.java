package com.moderation.sentinel.service.moderation;

import com.moderation.sentinel.model.ModerationResponse;
import com.moderation.sentinel.util.algorithm.normalization.TextNormalizer;
import com.moderation.sentinel.util.algorithm.trie.Trie;
import com.moderation.sentinel.util.algorithm.trie.TrieInitializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ModerationService {

    private final Trie offensiveTrie;
    private final Trie safeTrie;
    private static final double EXACT_MATCH_WEIGHT = 1.0;
    private static final double PHONETIC_MATCH_WEIGHT = 0.6;
    private static final double OBFUSCATION_MATCH_WEIGHT = 0.8;
    private static final double LEVENSHTEIN_MATCH_WEIGHT = 0.7;
    private static final double OFFENSIVE_THRESHOLD = 0.8;


    public ModerationService(Map<String, Trie> tries) {
        this.offensiveTrie = tries.get("offensive");
        this.safeTrie = tries.getOrDefault("safe", new Trie());
        // Initialize safeTrie with common safe words if empty

    }

    public ModerationResponse analyze(String input) {
        if (input == null || input.isBlank()) {
            return new ModerationResponse(false, 0.0, "Empty Input", Map.of());
        }

        // Normalize and tokenize input
        String normalized = TextNormalizer.normalize(input);
        List<String> tokens = TextNormalizer.tokenize(normalized);

        // Track offensive terms and their confidence scores
        Map<String, Double> offensiveTerms = new HashMap<>();
        double totalConfidence = 0.0;
        int matchCount = 0;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            // 1. Safe word check
            /*
                Implement Safe Word check algorithm here
             */

            // 2. Exact match
            Trie.DetectionResult exactMatch = offensiveTrie.contains(token);
            if (exactMatch.isOffensive) {
                offensiveTerms.put(token, EXACT_MATCH_WEIGHT);
                totalConfidence += EXACT_MATCH_WEIGHT;
                matchCount++;
                continue;
            }

            // 3. Phonetic match with context check
            String soundex = TrieInitializer.computeSoundex(token);
            Trie.DetectionResult phoneticMatch = offensiveTrie.containsPhonetic(soundex);
            if (phoneticMatch.isOffensive && isContextOffensive(tokens, i)) {
                offensiveTerms.put(token, PHONETIC_MATCH_WEIGHT);
                totalConfidence += PHONETIC_MATCH_WEIGHT;
                matchCount++;
                continue;
            }

            // 4. Obfuscation/Noise detection
            /*
                Implement the Noise Detection Algorithm
             */

            // 5. Levenshtein distance check
            /*
                Implement Levenshtein Algorithm her
             */
        }

        // Calculate final confidence (average weighted score)
        double confidence = matchCount > 0 ? totalConfidence / matchCount : 0.0;
        boolean isOffensive = confidence >= OFFENSIVE_THRESHOLD && !offensiveTerms.isEmpty();
        String message = isOffensive ? "Offensive terms found" : "Clean";

        return new ModerationResponse(isOffensive, confidence, message, offensiveTerms);
    }

    // Basic context check to reduce false positives
    private boolean isContextOffensive(List<String> tokens, int index) {
        // Check neighboring words for context (e.g., avoid flagging medical terms)
        int start = Math.max(0, index - 2);
        int end = Math.min(tokens.size(), index + 3);
        for (int i = start; i < end; i++) {
            if (i != index && safeTrie.contains(tokens.get(i)).isOffensive) {
                return false; // Safe context reduces likelihood of offense
            }
        }
        return true; // No safe context, assume potentially offensive
    }

}