package com.lowdragmc.lowdraglib.client.renderer;

import com.lowdragmc.lowdraglib.client.renderer.block.RendererBlock;
import com.lowdragmc.lowdraglib.client.renderer.block.RendererBlockEntity;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.ILDLRegisterClient;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurable;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.syncdata.IAutoPersistedSerializable;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;

public interface ISerializableRenderer extends IConfigurable, IRenderer, IAutoPersistedSerializable, ILDLRegisterClient {

    static CompoundTag serializeWrapper(HolderLookup.Provider provider, ISerializableRenderer renderer) {
        return renderer.serializeNBT(provider);
    }

    @Nullable
    static ISerializableRenderer deserializeWrapper(HolderLookup.Provider provider, CompoundTag tag) { return null; /* ported stub */ }

    // should be called after deserialization and only once.
    default void initRenderer() {

    }

    default void createPreview(ConfiguratorGroup father) {  /* ported stub */ }

    @Override
    default void buildConfigurator(ConfiguratorGroup father) {
        createPreview(father);
        IConfigurable.super.buildConfigurator(father);
    }
}
