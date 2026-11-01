package com.iafenvoy.jupiter.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ConfigDataFixer {
    private final Map<String, String> exactKeyMapper = new LinkedHashMap<>();
    private final Map<Pattern, Function<String, String>> regexKeyMapper = new LinkedHashMap<>();

    public void registerKeyRule(String oldKey, String newKey) {
        this.exactKeyMapper.put(oldKey, newKey);
    }

    public void registerKeyRule(String regex, Function<String, String> keyMapper) {
        this.registerKeyRule(Pattern.compile(regex), keyMapper);
    }

    public void registerKeyRule(Pattern regex, Function<String, String> keyMapper) {
        this.regexKeyMapper.put(regex, keyMapper);
    }

    public String fixKey(String old) {
        String newKey = this.exactKeyMapper.get(old);
        if (newKey != null) return newKey;
        for (Map.Entry<Pattern, Function<String, String>> entry : this.regexKeyMapper.entrySet())
            if (entry.getKey().matcher(old).find())
                return entry.getValue().apply(old);
        return old;
    }
}
