package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SearchComponentWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidConfigurator extends ValueConfigurator<Fluid> implements SearchComponentWidget.IWidgetSearch<Fluid> {
    protected SearchComponentWidget<Fluid> searchComponent;
    protected ImageWidget image;

    public FluidConfigurator(String name, Supplier<Fluid> supplier, Consumer<Fluid> onUpdate, @Nonnull Fluid defaultValue, boolean forceUpdate) {
        super(name, supplier, onUpdate, defaultValue, forceUpdate);
        if (value == null) {
            value = defaultValue;
        }
    }

    @Override
    protected void onValueUpdate(Fluid newValue) {
        if (newValue == null) newValue = defaultValue;
        if (value == newValue) return;
        super.onValueUpdate(newValue);
        searchComponent.setCurrentString(BuiltInRegistries.FLUID.getKey(value == null ? defaultValue : value).toString());
    }

    @Override
    public void init(int width) {  /* ported stub */ }


    @Override
    public String resultDisplay(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid).toString();
    }

    @Override
    public void selectResult(Fluid value) {
        onValueUpdate(value);
        updateValue();
    }

    @Override
    public void search(String word, Consumer<Fluid> find) {  /* ported stub */ }
}
