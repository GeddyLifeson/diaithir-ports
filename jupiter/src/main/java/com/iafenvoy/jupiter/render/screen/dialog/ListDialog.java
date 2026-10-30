package com.iafenvoy.jupiter.render.screen.dialog;

import com.iafenvoy.jupiter.config.entry.ListBaseEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.TitleStack;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collection;
import java.util.List;

public class ListDialog<T> extends AbstractListDialog<List<T>, T> {
    protected final ListBaseEntry<T> entry;

    public ListDialog(Screen parent, TitleStack titleStack, ConfigMetaProvider provider, ListBaseEntry<T> entry) {
        super(parent, titleStack, provider, entry);
        this.entry = entry;
    }

    @Override
    protected void addNewValue() {
        this.entry.getValue().add(this.entry.newValue());
    }

    @Override
    protected Collection<T> getValues() {
        return this.entry.getValue();
    }

    @Override
    protected ConfigEntry<T> newSingleInstance(T value, int index, Runnable reload) {
        return this.entry.newSingleInstance(value, index, reload);
    }
}
