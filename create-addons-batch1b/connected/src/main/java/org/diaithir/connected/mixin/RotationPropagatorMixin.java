package org.diaithir.connected.mixin;

import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import org.diaithir.connected.content.IConnectionForwardingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = RotationPropagator.class, remap = false)
public abstract class RotationPropagatorMixin {
    @Inject(method = "getPotentialNeighbourLocations", at = @At("RETURN"))
    private static void createConnected$forwardConnection(KineticBlockEntity be, CallbackInfoReturnable<List<BlockPos>> cir) {
        List<BlockPos> original = cir.getReturnValue();
        List<BlockPos> positions = new ArrayList<>(original);
        for (int i = 0; i < positions.size(); i++) {
            BlockPos sourcePos = be.getBlockPos();
            BlockPos neighborPos = positions.get(i);
            if (neighborPos.getClass() != BlockPos.class) continue;
            while (!sourcePos.equals(neighborPos) && be.getLevel().getBlockState(neighborPos).getBlock() instanceof IConnectionForwardingBlock fwd) {
                BlockPos tempSource = sourcePos;
                sourcePos = neighborPos;
                neighborPos = fwd.forwardConnection(be.getLevel(), tempSource, be.getLevel().getBlockState(tempSource), neighborPos);
            }
            positions.set(i, neighborPos);
        }
        original.clear();
        original.addAll(positions);
    }
}
