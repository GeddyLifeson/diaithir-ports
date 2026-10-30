package org.valkyrienskies.mod.mixin.feature.wave_spawning;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

// 26.2: Raid no longer stores a ServerLevel field - tick/findRandomSpawnPos/getValidSpawnPos all
// take the ServerLevel as a parameter instead. findRandomSpawnPos is now called from two
// callers (getValidSpawnPos directly, and a synthetic lambda inside tick), so instead of
// @WrapOperation-ing each call site we @WrapMethod the target method itself once.
@Mixin(Raid.class)
public abstract class MixinRaid {
    @Shadow
    public abstract BlockPos getCenter();

    @Shadow
    protected abstract void setCenter(BlockPos blockPos);

    @WrapMethod(method = "findRandomSpawnPos")
    private BlockPos onFindRandomSpawnPos(ServerLevel level, int i, Operation<BlockPos> original) {
        BlockPos originalCenter = this.getCenter();
        this.setCenter(BlockPos.containing(VSGameUtilsKt.toWorldCoordinates(level, originalCenter)));
        BlockPos result = original.call(level, i);
        this.setCenter(originalCenter);
        if (result != null) {
            return result;
        } else {
            return original.call(level, i);
        }
    }
}
