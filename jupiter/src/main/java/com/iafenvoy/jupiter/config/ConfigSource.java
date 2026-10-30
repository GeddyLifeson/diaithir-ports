package com.iafenvoy.jupiter.config;

import net.minecraft.network.chat.Component;

public record ConfigSource(Component name, int color, boolean jupiterCapability) {
    public static final ConfigSource NONE = new ConfigSource(Component.empty(), 0xFF7F7F7F, false);
    public static final ConfigSource JUPITER = new ConfigSource(Component.translatable("jupiter.config_source.jupiter", new Object[]{}), 0xFFDAD1B4, false);
    public static final ConfigSource NIGHT_CONFIG = new ConfigSource(Component.translatable("jupiter.config_source.night_config", new Object[]{}), 0xFFFFA500, true);
    public static final ConfigSource CLOTH_CONFIG = new ConfigSource(Component.translatable("jupiter.config_source.cloth_config", new Object[]{}), 0xFF9ACD32, true);
}
