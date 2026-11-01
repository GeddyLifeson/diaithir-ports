package org.valkyrienskies.mod.mixin.accessors.world.level.block;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// In 26.1 the TOP_SHAPES / BOTTOM_SHAPES arrays were replaced by per-shape direction maps
@Mixin(StairBlock.class)
public interface StairBlockAccessor {
    @Accessor("SHAPE_TOP_OUTER")
    static Map<Direction, VoxelShape> getShapeTopOuter() {
        throw new AssertionError();
    }

    @Accessor("SHAPE_TOP_STRAIGHT")
    static Map<Direction, VoxelShape> getShapeTopStraight() {
        throw new AssertionError();
    }

    @Accessor("SHAPE_TOP_INNER")
    static Map<Direction, VoxelShape> getShapeTopInner() {
        throw new AssertionError();
    }

    @Accessor("SHAPE_BOTTOM_OUTER")
    static Map<Direction, VoxelShape> getShapeBottomOuter() {
        throw new AssertionError();
    }

    @Accessor("SHAPE_BOTTOM_STRAIGHT")
    static Map<Direction, VoxelShape> getShapeBottomStraight() {
        throw new AssertionError();
    }

    @Accessor("SHAPE_BOTTOM_INNER")
    static Map<Direction, VoxelShape> getShapeBottomInner() {
        throw new AssertionError();
    }
}
