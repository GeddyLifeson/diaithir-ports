package org.valkyrienskies.mod.mixin.feature.fluid_escaping_ship_config;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.joml.primitives.AABBic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.config.VSGameConfig;

// 26.2: the old per-direction canSpreadTo(...) check was consolidated into a single
// getSpread(ServerLevel, BlockPos, BlockState): Map<Direction, FluidState> that computes all
// candidate directions at once. Filter out any candidate whose target position falls outside
// the bounds of the ship managing it, matching the old per-direction cancellation semantics.
@Mixin(FlowingFluid.class)
public class MixinFlowingFluid {

    @ModifyReturnValue(method = "getSpread", at = @At("RETURN"))
    private Map<Direction, FluidState> filterSpreadOutsideShip(Map<Direction, FluidState> original,
        ServerLevel level, BlockPos fromPos, BlockState state) {
        if (!VSGameConfig.SERVER.getPreventFluidEscapingShip()) {
            return original;
        }
        final Map<Direction, FluidState> filtered = new HashMap<>(original);
        filtered.entrySet().removeIf(entry -> {
            final BlockPos toPos = fromPos.relative(entry.getKey());
            final Ship ship = VSGameUtilsKt.getShipManagingPos(level, toPos);
            if (ship == null || ship.getShipAABB() == null) {
                return false;
            }
            final AABBic a = ship.getShipAABB();
            final int x = toPos.getX();
            final int y = toPos.getY();
            final int z = toPos.getZ();
            return x < a.minX() || y < a.minY() || z < a.minZ() || x >= a.maxX() || y >= a.maxY() || z >= a.maxZ();
        });
        return filtered;
    }
}
