package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class MovingBlockData {

    private BlockState state;
    private VoxelShape shape;
    private BlockPos offset;
    @Nullable
    public CompoundTag blockData;

    public MovingBlockData(BlockState state, VoxelShape shape, BlockPos offset, @Nullable CompoundTag blockData) {
        this.state = state;
        this.shape = shape;
        this.offset = offset;
        this.blockData = blockData;
    }

    public MovingBlockData(Level level, CompoundTag tag) {
        this(NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag.getCompoundOrEmpty("BlockState")), getShapeFromTag(tag.getCompoundOrEmpty("VoxelShape")), new BlockPos(tag.getIntOr("OffsetX", 0), tag.getIntOr("OffsetY", 0), tag.getIntOr("OffsetZ", 0)), tag.contains("BlockData") ? tag.getCompoundOrEmpty("BlockData") : null);
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public VoxelShape getShape() {
        return shape;
    }

    public void setShape(VoxelShape shape) {
        this.shape = shape;
    }

    public BlockPos getOffset() {
        return offset;
    }

    public void setOffset(BlockPos offset) {
        this.offset = offset;
    }

    @Nullable
    public CompoundTag getBlockData() {
        return blockData;
    }

    public void setBlockData(@Nullable CompoundTag blockData) {
        this.blockData = blockData;
    }

    public CompoundTag toTag() {
        CompoundTag data = new CompoundTag();
        data.put("BlockState", NbtUtils.writeBlockState(state));
        data.put("VoxelShape", getShapeTag());
        data.putInt("OffsetX", offset.getX());
        data.putInt("OffsetY", offset.getY());
        data.putInt("OffsetZ", offset.getZ());
        if (blockData != null) {
            data.put("BlockData", blockData);
        }
        return data;
    }

    private CompoundTag getShapeTag() {
        CompoundTag data = new CompoundTag();
        ListTag listTag = new ListTag();
        for (AABB shapeAABB : shape.toAabbs()) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("BoxMinX", shapeAABB.minX);
            tag.putDouble("BoxMinY", shapeAABB.minY);
            tag.putDouble("BoxMinZ", shapeAABB.minZ);
            tag.putDouble("BoxMaxX", shapeAABB.maxX);
            tag.putDouble("BoxMaxY", shapeAABB.maxY);
            tag.putDouble("BoxMaxZ", shapeAABB.maxZ);
            listTag.add(tag);
        }
        data.put("AABBs", listTag);
        return data;
    }

    private static VoxelShape getShapeFromTag(CompoundTag data) {
        VoxelShape shape = Shapes.empty();
        if (data.contains("AABBs")) {
            ListTag listtag = data.getListOrEmpty("AABBs");
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag innerTag = listtag.getCompoundOrEmpty(i);
                AABB aabb = new AABB(innerTag.getDoubleOr("BoxMinX", 0D), innerTag.getDoubleOr("BoxMinY", 0D), innerTag.getDoubleOr("BoxMinZ", 0D), innerTag.getDoubleOr("BoxMaxX", 0D), innerTag.getDoubleOr("BoxMaxY", 0D), innerTag.getDoubleOr("BoxMaxZ", 0D));
                shape = Shapes.join(shape, Shapes.create(aabb), BooleanOp.OR);
            }
        }
        return shape;
    }

}
