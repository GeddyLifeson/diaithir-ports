package com.lowdragmc.lowdraglib.syncdata.accessor;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.StringPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

/**
 * @author KilaBash
 * @date 2022/9/7
 * @implNote BlockStateAccessor
 */
public class ComponentAccessor extends CustomObjectAccessor<Component> {

    public ComponentAccessor() {
        super(Component.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, Component value, HolderLookup.Provider provider) {
        var ops = provider.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var json = net.minecraft.network.chat.ComponentSerialization.CODEC.encodeStart(ops, value).result().orElse(new com.google.gson.JsonPrimitive(""));
        return com.lowdragmc.lowdraglib.syncdata.payload.StringPayload.of(json.toString());
    }

    @Override
    public Component deserialize(AccessorOp op, ITypedPayload<?> payload, HolderLookup.Provider provider) {
        if (payload instanceof com.lowdragmc.lowdraglib.syncdata.payload.StringPayload stringPayload) {
            var ops = provider.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            return net.minecraft.network.chat.ComponentSerialization.CODEC.parse(ops, com.google.gson.JsonParser.parseString(stringPayload.getPayload())).result().orElse(null);
        }
        return null;
    }
}
