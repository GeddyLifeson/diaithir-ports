package org.valkyrienskies.mod.mixin.feature.distance_replace;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * 26.2: server-side interaction gating moved to {@code Player.isWithinBlockInteractionRange},
 * which measures {@code new AABB(pos).distanceToSqr(getEyePosition())} directly - it does NOT go
 * through {@code Entity.distanceToSqr}, so the ship-aware distance replacement in
 * {@link MixinEntity} never applies. Clicking any block on an assembled ship therefore failed
 * with "Rejecting UseItemOnPacket ... too far away from hit block" (the hit BlockPos is in
 * shipyard space, ~30M blocks from the player's world position).
 *
 * <p>When the vanilla check fails, re-test using the ship-aware distance between the player's eye
 * position and the block center; +1 covers the AABB-surface-vs-center discrepancy.
 */
@Mixin(Player.class)
public abstract class MixinPlayer {

    @ModifyReturnValue(method = "isWithinBlockInteractionRange", at = @At("RETURN"))
    private boolean vs$includeShipsInBlockInteractionRange(final boolean original, final BlockPos pos,
        final double padding) {
        if (original) {
            return true;
        }
        final Player self = Player.class.cast(this);
        final double range = self.blockInteractionRange() + padding + 1.0;
        final Vec3 eye = self.getEyePosition();
        final double distSq = VSGameUtilsKt.squaredDistanceBetweenInclShips(
            self.level(),
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            eye.x, eye.y, eye.z
        );
        return distSq < range * range;
    }
}
