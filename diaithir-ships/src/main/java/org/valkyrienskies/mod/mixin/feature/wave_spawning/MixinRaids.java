package org.valkyrienskies.mod.mixin.feature.wave_spawning;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(Raids.class)
public class MixinRaids {
    @Unique
    private ServerLevel vs$level;

    @Inject(method = "tick", at = @At("HEAD"))
    private void vs$captureLevel(final ServerLevel level, final CallbackInfo ci) {
        this.vs$level = level;
    }

    @WrapOperation(method = "getNearbyRaid", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/raid/Raid;getCenter()Lnet/minecraft/core/BlockPos;"))
    private BlockPos onGetNearbyRaid(Raid instance, Operation<BlockPos> original) {
        final BlockPos center = original.call(instance);
        if (this.vs$level == null) return center;
        return BlockPos.containing(VSGameUtilsKt.toWorldCoordinates(this.vs$level, center));
    }
}
