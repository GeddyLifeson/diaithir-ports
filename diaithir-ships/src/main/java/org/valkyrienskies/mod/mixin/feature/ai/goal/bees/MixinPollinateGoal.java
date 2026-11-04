package org.valkyrienskies.mod.mixin.feature.ai.goal.bees;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(targets = "net.minecraft.world.entity.animal.bee.Bee$BeePollinateGoal")
public abstract class MixinPollinateGoal {
    @Shadow
    @Final
    Bee this$0;

    @Shadow
    protected abstract Optional<BlockPos> findNearbyFlower();

    @Unique
    private BlockPos modifiedBeePosition = null;

    @Unique
    private boolean searchingShips = false;

    @Inject(method = "findNearbyFlower", at = @At("RETURN"), cancellable = true)
    private void preFindNearbyFlower(final CallbackInfoReturnable<Optional<BlockPos>> cir) {
        if (searchingShips || cir.getReturnValue().isPresent()) {
            return;
        }
        final BlockPos beePos = this$0.blockPosition();
        final List<BlockPos> found = new ArrayList<>();
        searchingShips = true;
        try {
            for (final var pos : VSGameUtilsKt.transformToNearbyShipsAndWorld(this$0.level(), beePos.getX(),
                beePos.getY(), beePos.getZ(), 5.0)) {
                this.modifiedBeePosition = BlockPos.containing(pos.x, pos.y, pos.z);
                findNearbyFlower().ifPresent(found::add);
            }
        } finally {
            modifiedBeePosition = null;
            searchingShips = false;
        }
        final Optional<BlockPos> res = found.stream().min(Comparator.comparingDouble(
            pos -> VSGameUtilsKt.squaredDistanceBetweenInclShips(this$0.level(),
                beePos.getX(), beePos.getY(), beePos.getZ(), pos.getX(), pos.getY(), pos.getZ())));
        cir.setReturnValue(res);
    }

    @WrapOperation(method = "findNearbyFlower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/bee/Bee;blockPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos onBlockPosition(Bee instance, Operation<BlockPos> original) {
        return modifiedBeePosition != null ? modifiedBeePosition : original.call(instance);
    }
}
