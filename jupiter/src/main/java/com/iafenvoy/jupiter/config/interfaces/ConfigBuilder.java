package com.iafenvoy.jupiter.config.interfaces;

import net.minecraft.network.chat.Component;

public interface ConfigBuilder<T, E extends ConfigEntry<T>, B extends ConfigBuilder<T, E, B>> {
    B tooltip(String tooltipKey);

    B tooltip(Component tooltipKey);

    B callback(ValueChangeCallback<T> callback);

    B value(T value);

    E build();

}
