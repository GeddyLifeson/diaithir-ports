package com.lowdragmc.lowdraglib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote RaytraceHelper, copied from create.
 */
public class RayTraceHelper {
    public static BlockHitResult rayTraceRange(Level worldIn, Player playerIn, double range) {
        Vec3 origin = getTraceOrigin(playerIn);
        Vec3 target = getTraceTarget(playerIn, range, origin);
        ClipContext context = new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, playerIn);
        return worldIn.clip(context);
    }

    public static PredicateTraceResult rayTraceUntil(Player playerIn, double range, Predicate<BlockPos> predicate) {
        Vec3 origin = getTraceOrigin(playerIn);
        Vec3 target = getTraceTarget(playerIn, range, origin);
        return rayTraceUntil(origin, target, predicate);
    }

    public static Vec3 getTraceTarget(Player playerIn, double range, Vec3 origin) {
        float f = playerIn.getXRot();
        float f1 = playerIn.getYRot();
        float f2 = Mth.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = Mth.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -Mth.cos(-f * 0.017453292F);
        float f5 = Mth.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        return origin.add((double) f6 * range, (double) f5 * range, (double) f7 * range);
    }

    public static Vec3 getTraceOrigin(Player playerIn) {
        double d0 = playerIn.getX();
        double d1 = playerIn.getY() + (double) playerIn.getEyeHeight();
        double d2 = playerIn.getZ();
        return new Vec3(d0, d1, d2);
    }

    public static PredicateTraceResult rayTraceUntil(Vec3 start, Vec3 end, Predicate<BlockPos> predicate) { return null; /* ported stub */ }

    public static class PredicateTraceResult {
        private BlockPos pos;
        private Direction facing;

        public PredicateTraceResult(BlockPos pos, Direction facing) {
            this.pos = pos;
            this.facing = facing;
        }

        public PredicateTraceResult() {
            // missed, no result
        }

        public Direction getFacing() {
            return facing;
        }

        public BlockPos getPos() {
            return pos;
        }

        public boolean missed() {
            return this.pos == null;
        }
    }
}
