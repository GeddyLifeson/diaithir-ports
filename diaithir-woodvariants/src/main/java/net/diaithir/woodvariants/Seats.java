package net.diaithir.woodvariants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Sitting down on a Seat block. */
public final class Seats {
    private Seats() {}

    public static InteractionResult sit(Level level, BlockPos pos, Player player, double seatHeight, float yaw) {
        if (player.isShiftKeyDown() || player.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (!level.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return mount(level, pos, player, seatHeight, yaw) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }

    /** Puts a seat at the block and seats the rider on it; the seat is removed again if the rider cannot board. */
    public static boolean mount(Level level, BlockPos pos, Entity rider, double seatHeight, float yaw) {
        SeatEntity seat = new SeatEntity(DiaithirWoodVariants.SEAT.get(), level);
        seat.setPos(pos.getX() + 0.5, pos.getY() + seatHeight, pos.getZ() + 0.5);
        seat.setYRot(yaw);
        level.addFreshEntity(seat);
        if (!rider.startRiding(seat)) {
            seat.discard();
            return false;
        }
        return true;
    }
}
