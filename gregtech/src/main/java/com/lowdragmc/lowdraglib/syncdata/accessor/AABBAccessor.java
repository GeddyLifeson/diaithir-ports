package com.lowdragmc.lowdraglib.syncdata.accessor;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

public class AABBAccessor extends CustomObjectAccessor<AABB> {

    public AABBAccessor() {
        super(AABB.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, AABB value, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("minX", value.minX);
        tag.putDouble("minY", value.minY);
        tag.putDouble("minZ", value.minZ);
        tag.putDouble("maxX", value.maxX);
        tag.putDouble("maxY", value.maxY);
        tag.putDouble("maxZ", value.maxZ);
        return NbtTagPayload.of(tag);
    }

    @Override
    public AABB deserialize(AccessorOp op, ITypedPayload<?> payload, HolderLookup.Provider provider) {
        if (payload instanceof NbtTagPayload nbtTagPayload && nbtTagPayload.getPayload() instanceof CompoundTag tag) {
            return new AABB(tag.getDoubleOr("minX", 0), tag.getDoubleOr("minY", 0), tag.getDoubleOr("minZ", 0),
                    tag.getDoubleOr("maxX", 0), tag.getDoubleOr("maxY", 0), tag.getDoubleOr("maxZ", 0));
        }
        return null;
    }
}
