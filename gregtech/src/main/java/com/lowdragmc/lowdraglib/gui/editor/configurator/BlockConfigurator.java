package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SearchComponentWidget;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockConfigurator extends ValueConfigurator<Block> implements SearchComponentWidget.IWidgetSearch<Block> {
    protected SearchComponentWidget<Block> searchComponent;
    protected ImageWidget image;

    public BlockConfigurator(String name, Supplier<Block> supplier, Consumer<Block> onUpdate, @Nonnull Block defaultValue, boolean forceUpdate) {
        super(name, supplier, onUpdate, defaultValue, forceUpdate);
        if (value == null) {
            value = defaultValue;
        }
    }

    @Override
    protected void onValueUpdate(Block newValue) {
        if (newValue == null) newValue = defaultValue;
        if (value == newValue) return;
        super.onValueUpdate(newValue);
        searchComponent.setCurrentString(BuiltInRegistries.BLOCK.getKey(value == null ? defaultValue : value).toString());
    }

    @Override
    public void init(int width) {  /* ported stub */ }


    @Override
    public String resultDisplay(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    @Override
    public void selectResult(Block block) {
        onValueUpdate(block);
        updateValue();
    }

    @Override
    public void search(String word, Consumer<Block> find) {  /* ported stub */ }
}
