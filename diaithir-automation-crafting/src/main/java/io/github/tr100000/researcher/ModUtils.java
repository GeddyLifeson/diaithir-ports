package io.github.tr100000.researcher;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;

public final class ModUtils {
    private ModUtils() {}

    @Contract(value = "_ -> new", pure = true)
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Researcher.MODID, path);
    }

    @Contract(pure = true)
    public static String getTranslationKey(String group, String key) {
        return String.format("%s.%s.%s", group, Researcher.MODID, key);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static MutableComponent getTranslated(String group, String key) {
        return Component.translatable(getTranslationKey(group, key));
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static MutableComponent getTranslated(String group, String key, Object... args) {
        return Component.translatable(getTranslationKey(group, key), args);
    }

    @Contract(pure = true)
    public static String getScreenTranslationKey(String key) {
        return getTranslationKey("screen", key);
    }

    @Contract(value = "_ -> new", pure = true)
    public static MutableComponent getScreenTranslated(String key) {
        return Component.translatable(getScreenTranslationKey(key));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static MutableComponent getScreenTranslated(String key, Object... args) {
        return Component.translatable(getScreenTranslationKey(key), args);
    }
}
