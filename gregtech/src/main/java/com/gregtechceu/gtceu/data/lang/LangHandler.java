package com.gregtechceu.gtceu.data.lang;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/** Runtime-only part of the upstream LangHandler (the data generation half is not ported). */
public class LangHandler {

    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }

    public static List<MutableComponent> getMultiLang(String key, Object... args) {
        List<MutableComponent> out = new ArrayList<>();
        int i = 0;
        String next = getSubKey(key, i);
        while (Language.getInstance().has(next)) {
            out.add(Component.translatable(next, args));
            next = getSubKey(key, ++i);
        }
        return out;
    }

    public static List<MutableComponent> getSingleOrMultiLang(String key, Object... args) {
        if (Language.getInstance().has(key)) {
            return List.of(Component.translatable(key, args));
        }
        return getMultiLang(key, args);
    }
}
