package org.valkyrienskies.mod.mixin.feature.wave_spawning;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(Raid.class)
public abstract class MixinRaid {
    @Shadow
    public abstract BlockPos getCenter();

    @Shadow
    protected abstract void setCenter(BlockPos blockPos);

    @WrapOperation(method = "getValidSpawnPos(Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;findRandomSpawnPos(Lnet/minecraft/server/level/ServerLevel;I)Lnet/minecraft/core/BlockPos;"))
    private BlockPos onFindRandomSpawnPos(Raid instance, ServerLevel level, int i, Operation<BlockPos> original) {
        BlockPos originalCenter = this.getCenter();
        this.setCenter(BlockPos.containing(VSGameUtilsKt.toWorldCoordinates(level, originalCenter)));
        BlockPos result = original.call(instance, level, i);
        this.setCenter(originalCenter);
        if (result != null) {
            return result;
        } else {
            return original.call(instance, level, i);
        }
    }

    @WrapOperation(method = "lambda$tick$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;findRandomSpawnPos(Lnet/minecraft/server/level/ServerLevel;I)Lnet/minecraft/core/BlockPos;"))
    private BlockPos onFindRandomSpawnPos2(Raid instance, ServerLevel level, int i, Operation<BlockPos> original) {
        BlockPos originalCenter = this.getCenter();
        this.setCenter(BlockPos.containing(VSGameUtilsKt.toWorldCoordinates(level, originalCenter)));
        BlockPos result = original.call(instance, level, i);
        this.setCenter(originalCenter);
        if (result != null) {
            return result;
        } else {
            return original.call(instance, level, i);
        }
    }
}
