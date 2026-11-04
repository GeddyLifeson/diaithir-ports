package com.iafenvoy.jupiter.internal;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.iafenvoy.jupiter.config.entry.EnumEntry;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public class JupiterSettings extends AutoInitConfigContainer {
    public static final JupiterSettings INSTANCE = new JupiterSettings();
    public final General general = new General();

    private JupiterSettings() {
        super(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "jupiter"), "jupiter.screen.config.title", "./config/jupiter.json");
    }

    private static String name(String category, String name) {
        return String.format(Locale.ROOT, "config.%s.%s.%s", Jupiter.MOD_ID, category, name);
    }

    private static String tooltip(String category, String name) {
        return String.format(Locale.ROOT, "config.%s.%s.%s.tooltip", Jupiter.MOD_ID, category, name);
    }

    public static class General extends AutoInitConfigCategoryBase {
        public final BooleanEntry loadForgeConfigs = BooleanEntry.builder(name("general", "loadForgeConfigs"), true).key("loadForgeConfigs").tooltip(tooltip("general", "loadForgeConfigs")).build();
        public final BooleanEntry loadClothConfigs = BooleanEntry.builder(name("general", "loadClothConfigs"), true).key("loadClothConfigs").tooltip(tooltip("general", "loadClothConfigs")).build();
        public final EnumEntry<ConfigButtonReplaceStrategy> configButtonReplacement = EnumEntry.builder(name("general", "configButtonReplacement"), ConfigButtonReplaceStrategy.UNAVAILABLE_ONLY).key("configButtonReplacement").restartRequired().build();
        public final BooleanEntry redirectAutoConfigScreen = BooleanEntry.builder(name("general", "redirectAutoConfigScreen"), false).key("redirectAutoConfigScreen").tooltip(tooltip("general", "redirectAutoConfigScreen")).build();

        private General() {
            super("general", "jupiter.screen.config.general");
        }
    }
}
