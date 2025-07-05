package com.moderation.sentinel.util.algorithm.levenshtein;

import java.util.*;

public class LevenshteinDistance {
    private static final int MAX_DISTANCE_THRESHOLD = 3;
    private static final double SIMILARITY_THRESHOLD = 0.7;
    
    // Early termination threshold
    public static int computeDistance(String s1, String s2, int maxDistance) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        if (s1.equals(s2)) return 0;
        
        int len1 = s1.length();
        int len2 = s2.length();
        
        if (Math.abs(len1 - len2) > maxDistance) return maxDistance + 1;
        
        int[] prev = new int[len2 + 1];
        int[] curr = new int[len2 + 1];

        for (int j = 0; j <= len2; j++) {
            prev[j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            curr[0] = i;
            int minInRow = i;
            
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                
                curr[j] = Math.min(
                    Math.min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                );
                
                minInRow = Math.min(minInRow, curr[j]);
            }
            
            if (minInRow > maxDistance) return maxDistance + 1;
            
            // Swap arrays
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        
        return prev[len2];
    }
    
    public static double computeSimilarity(String s1, String s2) {
        int distance = computeDistance(s1, s2, MAX_DISTANCE_THRESHOLD);
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - (double) distance / maxLen;
    }
    
    // Damerau-Levenshtein (Transposition Errors)
    public static int computeDamerauDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        int[][] matrix = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) matrix[i][0] = i;
        for (int j = 0; j <= len2; j++) matrix[0][j] = j;
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                
                matrix[i][j] = Math.min(
                    Math.min(matrix[i - 1][j] + 1, matrix[i][j - 1] + 1),
                    matrix[i - 1][j - 1] + cost
                );
                
                if (i > 1 && j > 1 && 
                    s1.charAt(i - 1) == s2.charAt(j - 2) && 
                    s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    matrix[i][j] = Math.min(matrix[i][j], matrix[i - 2][j - 2] + cost);
                }
            }
        }
        
        return matrix[len1][len2];
    }
    
    public static List<String> findSimilarWords(String target, Set<String> wordSet, double threshold) {
        return wordSet.parallelStream()
            .filter(word -> computeSimilarity(target, word) >= threshold)
            .sorted((w1, w2) -> Double.compare(
                computeSimilarity(target, w2), 
                computeSimilarity(target, w1)
            ))
            .limit(10)
            .toList();
    }
}