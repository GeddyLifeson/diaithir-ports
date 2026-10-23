package com.iafenvoy.jupiter.util;

import java.util.Arrays;
import java.util.Comparator;

public class TextFormatter {
    public static String formatToTitleCase(String input, boolean ignoreTranslateKey) {
        if (input == null || input.trim().isEmpty()) return "";
        if (input.contains("."))
            if (ignoreTranslateKey) return input;//Do not format translate key
            else input = Arrays.stream(input.split("\\.")).max(Comparator.naturalOrder()).orElse("");
        String[] words = input.replaceAll("_", " ").replaceAll("([a-z])([A-Z])", "$1 $2").split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
                if (i < words.length - 1)
                    result.append(" ");
            }
        }
        return result.toString();
    }
}
