package com.lowdragmc.lowdraglib.syncdata.accessor;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.UIResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * @author KilaBash
 * @date 2022/9/7
 * @implNote BlockStateAccessor
 */
public class IGuiTextureAccessor extends CustomObjectAccessor<IGuiTexture>{

    public IGuiTextureAccessor() {
        super(IGuiTexture.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, IGuiTexture value, HolderLookup.Provider provider) {
        var tag = IGuiTexture.serializeWrapper(value, provider);
        if (tag == null) {
            tag = new CompoundTag();
            if (value instanceof UIResourceTexture uiResourceTexture) {
                tag.putString("type", "ui_resource");
                tag.putString("key", uiResourceTexture.key);
            } else {
                tag.putString("type", "empty");
            }
        }
        return NbtTagPayload.of(tag);
    }

    @Override
    public IGuiTexture deserialize(AccessorOp op, ITypedPayload<?> payload, HolderLookup.Provider provider) { return null; /* ported stub */ }

}
