package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.client.renderer.block.RendererBlock;
import com.lowdragmc.lowdraglib.client.renderer.block.RendererBlockEntity;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IRendererConfigurator extends ValueConfigurator<IRenderer>{
    private ImageWidget frame;
    private RendererBlockEntity holder;

    public IRendererConfigurator(String name, Supplier<IRenderer> supplier, Consumer<IRenderer> onUpdate, IRenderer defaultRenderer, boolean forceUpdate) {
        super(name, supplier, onUpdate, defaultRenderer, forceUpdate);
        if (value == null) {
            value = defaultValue;
        }
    }

    @Override
    protected void onValueUpdate(IRenderer newValue) {  /* ported stub */ }

    @Override
    public void computeHeight() {  /* ported stub */ }

    @Override
    public void init(int width) {  /* ported stub */ }

}
