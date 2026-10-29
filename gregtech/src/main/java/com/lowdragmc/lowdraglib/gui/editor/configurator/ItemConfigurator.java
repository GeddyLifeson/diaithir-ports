package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SearchComponentWidget;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemConfigurator extends ValueConfigurator<Item> implements SearchComponentWidget.IWidgetSearch<Item> {
    protected SearchComponentWidget<Item> searchComponent;
    protected ImageWidget image;

    public ItemConfigurator(String name, Supplier<Item> supplier, Consumer<Item> onUpdate, @Nonnull Item defaultValue, boolean forceUpdate) {
        super(name, supplier, onUpdate, defaultValue, forceUpdate);
        if (value == null) {
            value = defaultValue;
        }
    }

    @Override
    protected void onValueUpdate(Item newValue) {
        if (newValue == null) newValue = defaultValue;
        if (value == newValue) return;
        super.onValueUpdate(newValue);
        searchComponent.setCurrentString(BuiltInRegistries.ITEM.getKey(value == null ? defaultValue : value).toString());
    }

    @Override
    public void init(int width) {  /* ported stub */ }


    @Override
    public String resultDisplay(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    @Override
    public void selectResult(Item item) {
        onValueUpdate(item);
        updateValue();
    }

    @Override
    public void search(String word, Consumer<Item> find) {  /* ported stub */ }
}
