package com.lowdragmc.lowdraglib.syncdata.accessor;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.StringPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

/**
 * @author KilaBash
 * @date 2022/9/7
 * @implNote BlockStateAccessor
 */
public class ResourceLocationAccessor extends CustomObjectAccessor<Identifier>{

    public ResourceLocationAccessor() {
        super(Identifier.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, Identifier value, HolderLookup.Provider provider) {
        return StringPayload.of(value.toString());
    }

    @Override
    public Identifier deserialize(AccessorOp op, ITypedPayload<?> payload, HolderLookup.Provider provider) {
        if (payload instanceof StringPayload stringPayload) {
            return Identifier.parse(stringPayload.getPayload());
        }
        return null;
    }
}
