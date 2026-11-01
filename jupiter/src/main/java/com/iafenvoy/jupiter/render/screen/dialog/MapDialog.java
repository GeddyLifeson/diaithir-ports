package com.iafenvoy.jupiter.render.screen.dialog;

import com.iafenvoy.jupiter.config.entry.MapBaseEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.TitleStack;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collection;
import java.util.Map;

public class MapDialog<T> extends AbstractListDialog<Map<String, T>, Map.Entry<String, T>> {
    protected final MapBaseEntry<T> entry;

    public MapDialog(Screen parent, TitleStack titleStack, ConfigMetaProvider provider, MapBaseEntry<T> entry) {
        super(parent, titleStack, provider, entry);
        this.entry = entry;
    }

    @Override
    protected void addNewValue() {
        this.entry.getValue().put("", this.entry.newValue());
    }

    @Override
    protected Collection<Map.Entry<String, T>> getValues() {
        return this.entry.getValue().entrySet();
    }

    @Override
    protected ConfigEntry<Map.Entry<String, T>> newSingleInstance(Map.Entry<String, T> value, int index, Runnable reload) {
        return this.entry.newSingleInstance(value.getValue(), value.getKey(), reload);
    }
}
