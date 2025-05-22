package com.moderation.sentinel.util.algorithm.normalization;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class TextNormalizer {
    private static final Map<String, Character> substitutionMap = new HashMap<>();

    static {
        substitutionMap.put("@", 'a'); substitutionMap.put("4", 'a'); substitutionMap.put("^", 'a');
        substitutionMap.put("/\\", 'a'); substitutionMap.put("Δ", 'a'); substitutionMap.put("α", 'a');
        substitutionMap.put("ª", 'a'); substitutionMap.put("à", 'a'); substitutionMap.put("â", 'a');
        substitutionMap.put("8", 'b'); substitutionMap.put("ß", 'b');
        substitutionMap.put("(", 'c'); substitutionMap.put("<", 'c'); substitutionMap.put("¢", 'c');
        substitutionMap.put("{", 'c'); substitutionMap.put("©", 'c');
        substitutionMap.put("3", 'e'); substitutionMap.put("€", 'e'); substitutionMap.put("£", 'e');
        substitutionMap.put("ê", 'e'); substitutionMap.put("ë", 'e');
        substitutionMap.put("ph", 'f'); substitutionMap.put("ƒ", 'f');
        substitutionMap.put("6", 'g'); substitutionMap.put("9", 'g'); substitutionMap.put("&", 'g');
        substitutionMap.put("#", 'h');
        substitutionMap.put("1", 'i'); substitutionMap.put("!", 'i');
        substitutionMap.put("ĵ", 'j'); substitutionMap.put("ʝ", 'j');
        substitutionMap.put("/\\/\\", 'm'); substitutionMap.put("^^", 'm'); substitutionMap.put("м", 'm');
        substitutionMap.put("0", 'o'); substitutionMap.put("()", 'o'); substitutionMap.put("°", 'o');
        substitutionMap.put("ø", 'o'); substitutionMap.put("ö", 'o'); substitutionMap.put("ô", 'o');
        substitutionMap.put("φ", 'q'); substitutionMap.put("ɋ", 'q');
        substitutionMap.put("$", 's'); substitutionMap.put("5", 's'); substitutionMap.put("§", 's'); substitutionMap.put("ŝ", 's');
        substitutionMap.put("7", 't'); substitutionMap.put("+", 't');
        substitutionMap.put("\\/", 'v'); substitutionMap.put("√", 'v'); substitutionMap.put("ѵ", 'v');
        substitutionMap.put("\\/\\/", 'w'); substitutionMap.put("vv", 'w'); substitutionMap.put("ш", 'w');
        substitutionMap.put("ω", 'w'); substitutionMap.put("ŵ", 'w');
        substitutionMap.put("¥", 'y'); substitutionMap.put("j", 'y'); substitutionMap.put("ý", 'y'); substitutionMap.put("ʎ", 'y');
        substitutionMap.put("2", 'z'); substitutionMap.put("ζ", 'z'); substitutionMap.put("ʐ", 'z'); substitutionMap.put("ž", 'z');
    }

    public static String normalize(String input) {
        if (input == null || input.isEmpty()) return "";

        String result = input.toLowerCase();
        result = Normalizer.normalize(result, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");

        for (Map.Entry<String, Character> entry : substitutionMap.entrySet()) {
            result = result.replaceAll(Pattern.quote(entry.getKey()), entry.getValue().toString());
        }

        result = result.replaceAll("[*#_]", "");
        result = normalizeRepeatedCharacters(result, 2);

        return result;
    }

    public static List<String> tokenize(String input) {
        if (input == null || input.isEmpty()) return List.of();
        String[] tokens = input.replace("_", " ").split("\\s+");
        return new ArrayList<>(Arrays.asList(tokens));
    }

    private static String normalizeRepeatedCharacters(String input, int maxRepeats) {
        if (input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        int count = 1;
        result.append(input.charAt(0));

        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == input.charAt(i - 1)) {
                count++;
                if (count <= maxRepeats) {
                    result.append(input.charAt(i));
                }
            } else {
                count = 1;
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }
}