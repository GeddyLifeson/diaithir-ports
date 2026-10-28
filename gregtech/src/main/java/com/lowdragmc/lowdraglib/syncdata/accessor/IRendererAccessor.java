package com.lowdragmc.lowdraglib.syncdata.accessor;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.client.renderer.ISerializableRenderer;
import com.lowdragmc.lowdraglib.client.renderer.impl.UIResourceRenderer;
import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class IRendererAccessor extends CustomObjectAccessor<IRenderer> {

    public IRendererAccessor() {
        super(IRenderer.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, IRenderer value, HolderLookup.Provider provider) {
        if (value instanceof ISerializableRenderer serializableRenderer) {
            return NbtTagPayload.of(ISerializableRenderer.serializeWrapper(provider, serializableRenderer));
        } else if (value instanceof UIResourceRenderer renderer) {
            var tag = new CompoundTag();
            tag.putString("_type", "ui_resource");
            tag.putString("key", renderer.key);
            return NbtTagPayload.of(tag);
        }
        return NbtTagPayload.of(new CompoundTag());
    }

    @Override
    public IRenderer deserialize(AccessorOp op, ITypedPayload<?> payload, HolderLookup.Provider provider) { return null; /* ported stub */ }

}
