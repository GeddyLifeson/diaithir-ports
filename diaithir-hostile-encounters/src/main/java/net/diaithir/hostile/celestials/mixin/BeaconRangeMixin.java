package net.diaithir.hostile.celestials.mixin;

import net.diaithir.hostile.celestials.LunarEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Beacon range follows the active lunar event's beacon_radius (blue moons widen it). */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconRangeMixin {
    @ModifyVariable(method = "applyEffects", at = @At("STORE"), ordinal = 0, require = 1)
    private static double diaithirhostile$range(double range, Level level, BlockPos pos, int levels, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary) {
        return range * LunarEvents.beaconMultiplier(level);
    }
}
