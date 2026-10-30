package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;

import java.util.Map;

public abstract class EntryBaseEntry<T> extends BaseEntry<Map.Entry<String, T>> {
    protected EntryBaseEntry(Builder<Map.Entry<String, T>, ?, ?> builder) {
        super(builder);
    }

    public abstract ConfigEntry<T> newValueInstance();
}
