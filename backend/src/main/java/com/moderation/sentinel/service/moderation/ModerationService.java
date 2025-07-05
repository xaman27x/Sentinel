package com.moderation.sentinel.service.moderation;

import com.moderation.sentinel.model.ModerationResponse;
import com.moderation.sentinel.util.algorithm.normalization.TextNormalizer;
import com.moderation.sentinel.util.algorithm.trie.Trie;
import com.moderation.sentinel.util.algorithm.trie.TrieInitializer;
import com.moderation.sentinel.util.algorithm.levenshtein.LevenshteinDistance;
import com.moderation.sentinel.util.algorithm.noise.NoiseDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ModerationService {
    private final Trie offensiveTrie;
    private final Trie safeTrie;
    private final Map<String, Double> wordScoreCache = new ConcurrentHashMap<>();
    
    // Weight parameters for confidence calculation
    private static final double EXACT_MATCH_WEIGHT = 1.0;
    private static final double PHONETIC_MATCH_WEIGHT = 0.85;
    private static final double OBFUSCATION_MATCH_WEIGHT = 0.90;
    private static final double LEVENSHTEIN_MATCH_WEIGHT = 0.75;
    private static final double FUZZY_MATCH_WEIGHT = 0.70;
    private static final double CONTEXT_PENALTY = 0.3;
    private static final double OFFENSIVE_THRESHOLD = 0.7;
    private static final int MAX_LEVENSHTEIN_DISTANCE = 2;
    private static final int MAX_FUZZY_DISTANCE = 3;

    @Autowired
    public ModerationService(TrieInitializer trieInitializer) {
        this.offensiveTrie = trieInitializer.getOffensiveTrie();
        this.safeTrie = trieInitializer.getSafeTrie();
    }

    public ModerationResponse analyze(String input) {
        if (input == null || input.isBlank()) {
            return new ModerationResponse(false, 0.0, "Empty Input", Map.of());
        }

        AnalysisResult result = performComprehensiveAnalysis(input);
        
        boolean isOffensive = result.maxConfidence >= OFFENSIVE_THRESHOLD && !result.offensiveTerms.isEmpty();
        String message = generateDetailedMessage(result, isOffensive);
        
        return new ModerationResponse(
            isOffensive, 
            result.maxConfidence, 
            message, 
            result.offensiveTerms
        );
    }
    
    private AnalysisResult performComprehensiveAnalysis(String input) {
        String normalized = TextNormalizer.normalize(input);
        List<String> tokens = TextNormalizer.tokenize(normalized);
        
        Map<String, Double> offensiveTerms = new ConcurrentHashMap<>();
        double totalConfidence = 0.0;
        int detectionCount = 0;


        for (int i = 0; i < tokens.size(); i++) {
            DetectionTask task = new DetectionTask(tokens.get(i), i, tokens);
            DetectionResult result = analyzeToken(task);
            if (result.isOffensive) {
                offensiveTerms.put(result.originalToken, result.confidence);
                totalConfidence += result.confidence;
                detectionCount++;
            }
        }
        
        double finalConfidence = detectionCount > 0 ? 
            (totalConfidence / detectionCount) * calculateSeverityMultiplier(offensiveTerms.size(), tokens.size()) : 0.0;
            
        return new AnalysisResult(
            Math.min(1.0, finalConfidence),
            finalConfidence,
            offensiveTerms
        );
    }
    
    private DetectionResult analyzeToken(DetectionTask task) {
        String token = task.token;
        
        // This checks cache first
        if (wordScoreCache.containsKey(token)) {
            double cachedScore = wordScoreCache.get(token);
            return new DetectionResult(cachedScore > 0, cachedScore, token);
        }
        
        double maxConfidence = 0.0;
        boolean isOffensive = false;
        
        // 1. Safe word bypass
        if (isSafeWordContext(task)) {
            wordScoreCache.put(token, 0.0);
            return new DetectionResult(false, 0.0, token);
        }
        
        // 2. Exact match against Trie
        Trie.DetectionResult exactMatch = offensiveTrie.contains(token);
        if (exactMatch.isOffensive) {
            maxConfidence = EXACT_MATCH_WEIGHT * exactMatch.confidence;
            isOffensive = true;
        }
        
        // 3. Phonetic matching using Soundex
        if (!isOffensive) {
            String soundex = TrieInitializer.computeSoundex(token);
            Trie.DetectionResult phoneticMatch = offensiveTrie.containsPhonetic(soundex);
            if (phoneticMatch.isOffensive) {
                double phoneticConfidence = PHONETIC_MATCH_WEIGHT * phoneticMatch.confidence;
                if (phoneticConfidence > maxConfidence) {
                    maxConfidence = phoneticConfidence;
                    isOffensive = true;
                }
            }
        }
        
        // 4. Obfuscation detection
        if (!isOffensive) {
            String deobfuscated = NoiseDetector.removeObfuscation(token);
            if (!deobfuscated.equals(token)) {
                Trie.DetectionResult obfuscatedMatch = offensiveTrie.contains(deobfuscated);
                if (obfuscatedMatch.isOffensive) {
                    double obfuscationScore = NoiseDetector.calculateObfuscationScore(token, deobfuscated);
                    double obfuscationConfidence = OBFUSCATION_MATCH_WEIGHT * obfuscatedMatch.confidence * (1.0 - obfuscationScore * 0.3);
                    if (obfuscationConfidence > maxConfidence) {
                        maxConfidence = obfuscationConfidence;
                        isOffensive = true;
                    }
                }
            }
        }
        
        // 5. Levenshtein distance matching
        if (!isOffensive) {
            Set<String> offensiveWords = offensiveTrie.getAllWords();
            List<String> similarWords = LevenshteinDistance.findSimilarWords(token, offensiveWords, 0.7);
            
            for (String similarWord : similarWords) {
                int distance = LevenshteinDistance.computeDistance(token, similarWord, MAX_LEVENSHTEIN_DISTANCE);
                if (distance <= MAX_LEVENSHTEIN_DISTANCE) {
                    double similarity = LevenshteinDistance.computeSimilarity(token, similarWord);
                    double levenshteinConfidence = LEVENSHTEIN_MATCH_WEIGHT * similarity;
                    if (levenshteinConfidence > maxConfidence) {
                        maxConfidence = levenshteinConfidence;
                        isOffensive = true;
                    }
                }
            }
        }
        
        // 6. Fuzzy search within Trie
        if (!isOffensive) {
            List<Trie.DetectionResult> fuzzyResults = offensiveTrie.fuzzySearch(token, MAX_FUZZY_DISTANCE);
            for (Trie.DetectionResult fuzzyResult : fuzzyResults) {
                if (fuzzyResult.isOffensive) {
                    double fuzzyConfidence = FUZZY_MATCH_WEIGHT * fuzzyResult.confidence;
                    if (fuzzyConfidence > maxConfidence) {
                        maxConfidence = fuzzyConfidence;
                        isOffensive = true;
                        break;
                    }
                }
            }
        }
        
        // Context penalty
        if (isOffensive && hasNegativeContext(task)) {
            maxConfidence *= (1.0 - CONTEXT_PENALTY);
        }
        
        // Result is cached
        wordScoreCache.put(token, isOffensive ? maxConfidence : 0.0);
        
        return new DetectionResult(isOffensive, maxConfidence, token);
    }
    
    private boolean isSafeWordContext(DetectionTask task) {
        String token = task.token;
        List<String> tokens = task.allTokens;
        int index = task.index;

        if (safeTrie.contains(token).isOffensive) return true;


        int contextWindow = 2;
        for (int i = Math.max(0, index - contextWindow); 
             i < Math.min(tokens.size(), index + contextWindow + 1); i++) {
            if (i != index && safeTrie.contains(tokens.get(i)).isOffensive) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasNegativeContext(DetectionTask task) {
        List<String> negationWords = List.of("not", "never", "without", "except", "but", "neither", "nor", "hardly", "barely");
        List<String> tokens = task.allTokens;
        int index = task.index;
        
        // This checks for Negative Context in the Fixed Window Length(2)
        for (int i = Math.max(0, index - 2); i < Math.min(tokens.size(), index + 2); i++) {
            if (negationWords.contains(tokens.get(i).toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    private double calculateSeverityMultiplier(int offensiveCount, int totalTokens) {
        if (totalTokens == 0) return 1.0;
        double density = (double) offensiveCount / totalTokens;
        return Math.min(1.5, 1.0 + density); // Density Booster
    }
    
    private String generateDetailedMessage(AnalysisResult result, boolean isOffensive) {
        if (!isOffensive) return "Content appears clean";
        
        int termCount = result.offensiveTerms.size();
        double avgConfidence = result.averageConfidence;
        
        if (avgConfidence >= 0.9) return String.format("High confidence content flagged (%d terms)", termCount);
        if (avgConfidence >= 0.7) return String.format("Moderate confidence content flagged (%d terms)", termCount);
        return String.format("Low confidence content flagged (%d terms)", termCount);
    }
    
    // Helper classes
    private static class DetectionTask {
        final String token;
        final int index;
        final List<String> allTokens;
        
        DetectionTask(String token, int index, List<String> allTokens) {
            this.token = token;
            this.index = index;
            this.allTokens = allTokens;
        }
    }
    
    private static class DetectionResult {
        final boolean isOffensive;
        final double confidence;
        final String originalToken;
        
        DetectionResult(boolean isOffensive, double confidence, String originalToken) {
            this.isOffensive = isOffensive;
            this.confidence = confidence;
            this.originalToken = originalToken;
        }
    }
    
    private static class AnalysisResult {
        final double maxConfidence;
        final double averageConfidence;
        final Map<String, Double> offensiveTerms;
        
        AnalysisResult(double maxConfidence, double averageConfidence, Map<String, Double> offensiveTerms) {
            this.maxConfidence = maxConfidence;
            this.averageConfidence = averageConfidence;
            this.offensiveTerms = offensiveTerms;
        }
    }
}