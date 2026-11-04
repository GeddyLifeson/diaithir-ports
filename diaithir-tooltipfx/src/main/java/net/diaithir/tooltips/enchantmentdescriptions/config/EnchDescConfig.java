// Ported from Enchantment Descriptions (LGPL-2.1-only) https://github.com/Darkhax-Minecraft/Enchantment-Descriptions
// Modifications: Prickle @Value config replaced by ModConfigSpec; Component/Style options expressed as strings and booleans.
package net.diaithir.tooltips.enchantmentdescriptions.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class EnchDescConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue ONLY_ON_BOOKS;
    public static final ModConfigSpec.BooleanValue ONLY_IN_ENCHANTING_TABLE;
    public static final ModConfigSpec.BooleanValue REQUIRE_KEYBIND;
    public static final ModConfigSpec.ConfigValue<String> ACTIVATE_TEXT;
    public static final ModConfigSpec.BooleanValue DISPLAY_ACTIVATE_TEXT;
    public static final ModConfigSpec.ConfigValue<String> PREFIX;
    public static final ModConfigSpec.ConfigValue<String> SUFFIX;
    public static final ModConfigSpec.ConfigValue<String> STYLE_COLOR;
    public static final ModConfigSpec.BooleanValue STYLE_BOLD;
    public static final ModConfigSpec.BooleanValue STYLE_ITALIC;
    public static final ModConfigSpec.BooleanValue STYLE_UNDERLINED;
    public static final ModConfigSpec.BooleanValue STYLE_STRIKETHROUGH;
    public static final ModConfigSpec.BooleanValue STYLE_OBFUSCATED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Determines if the mod and its features are enabled.")
                .define("enabled", true);
        ONLY_ON_BOOKS = b.comment("When enabled, descriptions will only be displayed when looking at an enchanted book.")
                .define("only_on_books", false);
        ONLY_IN_ENCHANTING_TABLE = b.comment("When enabled, descriptions will only be displayed when inside the enchanting table GUI.")
                .define("only_in_enchanting_table", false);
        REQUIRE_KEYBIND = b.comment("When enabled, descriptions will only be displayed when the user holds the shift key.")
                .define("require_keybind", false);
        ACTIVATE_TEXT = b.comment("Text shown when require_keybind is enabled and the key is not held. A translation key or plain text.")
                .define("activate_text", "enchdesc.activate.message");
        DISPLAY_ACTIVATE_TEXT = b.comment("Should the activate text be displayed?")
                .define("display_activate_text", true);
        PREFIX = b.comment("Text added to the start of each description (e.g. spaces for indentation).")
                .define("prefix", "");
        SUFFIX = b.comment("Text added to the end of each description.")
                .define("suffix", "");
        STYLE_COLOR = b.comment("Color of the description text: a vanilla color name (e.g. dark_gray) or #RRGGBB. Empty = no color.")
                .define("style_color", "dark_gray");
        STYLE_BOLD = b.define("style_bold", false);
        STYLE_ITALIC = b.define("style_italic", false);
        STYLE_UNDERLINED = b.define("style_underlined", false);
        STYLE_STRIKETHROUGH = b.define("style_strikethrough", false);
        STYLE_OBFUSCATED = b.define("style_obfuscated", false);
        SPEC = b.build();
    }

    private EnchDescConfig() {}
}
