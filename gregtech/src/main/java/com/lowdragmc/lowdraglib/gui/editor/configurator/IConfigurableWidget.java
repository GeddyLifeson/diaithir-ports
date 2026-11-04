package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.editor.data.resource.Resource;
import com.lowdragmc.lowdraglib.gui.editor.data.resource.TexturesResource;

import com.lowdragmc.lowdraglib.gui.editor.runtime.PersistedParser;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.UIResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/6
 * @implNote IConfigurableWidget
 */
public interface IConfigurableWidget extends IConfigurable {/* ported: removed */

    default Widget widget() {
        return (Widget) this;
    }

    default void initTemplate() {

    }

    default boolean canDragIn(Object dragging) {
        if (dragging instanceof IGuiTexture) {
            return true;
        } else if (dragging instanceof String) {
            return true;
        } else if (dragging instanceof IIdProvider) {
            return true;
        } else if (dragging instanceof Integer) {
            return true;
        }
        return false;
    }

    default boolean handleDragging(Object dragging) { return false; /* ported stub */ }

    @SuppressWarnings("unchecked")
    static CompoundTag serializeNBT(IConfigurableWidget widget, Resources resources, boolean isProject, HolderLookup.Provider provider) {
        return serializeNBT(widget, (Resource<IGuiTexture>) resources.resources.get(TexturesResource.RESOURCE_NAME), isProject, provider);
    }

    static CompoundTag serializeNBT(IConfigurableWidget widget, Resource<IGuiTexture> resources, boolean isProject, HolderLookup.Provider provider) {
        UIResourceTexture.setCurrentResource(resources, isProject);
        CompoundTag tag = widget.serializeInnerNBT(provider);
        UIResourceTexture.clearCurrentResource();
        return tag;
    }

    @SuppressWarnings("unchecked")
    static void deserializeNBT(IConfigurableWidget widget, CompoundTag tag, Resources resources, boolean isProject, HolderLookup.Provider provider) {
        deserializeNBT(widget, tag, (Resource<IGuiTexture>) resources.resources.get(TexturesResource.RESOURCE_NAME), isProject, provider);
    }

    static void deserializeNBT(IConfigurableWidget widget, CompoundTag tag, Resource<IGuiTexture> resources, boolean isProject, HolderLookup.Provider provider) {
        UIResourceTexture.setCurrentResource(resources, isProject);
        widget.deserializeInnerNBT(provider, tag);
        UIResourceTexture.clearCurrentResource();
    }

    default CompoundTag serializeInnerNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        PersistedParser.serializeNBT(tag, getClass(), this, provider);
        return tag;
    }

    default void deserializeInnerNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        PersistedParser.deserializeNBT(nbt, new HashMap<>(), getClass(), this, provider);
    }

    default CompoundTag serializeWrapper(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString("type", name());
        tag.put("data", serializeInnerNBT(provider));
        return tag;
    }

    @Nullable
    static IConfigurableWidget deserializeWrapper(CompoundTag tag, HolderLookup.Provider provider) { return null; /* ported stub */ }

    // ******* setter ********//


    @FunctionalInterface
    interface IIdProvider extends Supplier<String> {

    }
}
