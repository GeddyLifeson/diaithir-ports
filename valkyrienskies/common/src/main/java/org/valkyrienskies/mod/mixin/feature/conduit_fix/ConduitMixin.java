package org.valkyrienskies.mod.mixin.feature.conduit_fix;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(ConduitBlockEntity.class)
public class ConduitMixin extends BlockEntity {

    public ConduitMixin(final BlockEntityType<?> blockEntityType, final BlockPos blockPos,
        final BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Redirect(
        method = "applyEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"
        ),
        require = 0
    )
    private static boolean closerThan(final BlockPos instance, final Vec3i vec3i, final double distance,
        final Level level, final BlockPos blockPos, final List<BlockPos> list) {
        final double retValue =
            VSGameUtilsKt.squaredDistanceBetweenInclShips(level, instance.getX(), instance.getY(), instance.getZ(),
                vec3i.getX(),
                vec3i.getY(),
                vec3i.getZ());
        return retValue < distance * distance;
    }

    // TODO Phase 4: 26.2 restructured updateDestroyTarget()'s internals around this closerThan()
    // call site (now captures EntityReference/ServerLevel/boolean locals instead of
    // Level/BlockState/List<BlockPos>/ConduitBlockEntity), so this redirect's local-capture
    // signature no longer matches despite require=0 (a signature mismatch on a located target is
    // a hard error, distinct from "target not found"). Dropped for now - conduit destroy-target
    // selection won't be ships-aware until this is redesigned against the new locals.
}
