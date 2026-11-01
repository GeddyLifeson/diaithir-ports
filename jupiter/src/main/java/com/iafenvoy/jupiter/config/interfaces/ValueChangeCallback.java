package com.iafenvoy.jupiter.config.interfaces;

@FunctionalInterface
public
interface ValueChangeCallback<T> {
    void onValueChange(T value, boolean reset, boolean isDefault);
}
