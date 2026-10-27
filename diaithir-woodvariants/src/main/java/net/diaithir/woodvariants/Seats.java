package net.diaithir.woodvariants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
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
        SeatEntity seat = new SeatEntity(DiaithirWoodVariants.SEAT.get(), level);
        seat.setPos(pos.getX() + 0.5, pos.getY() + seatHeight, pos.getZ() + 0.5);
        seat.setYRot(yaw);
        level.addFreshEntity(seat);
        player.startRiding(seat);
        return InteractionResult.SUCCESS_SERVER;
    }
}
