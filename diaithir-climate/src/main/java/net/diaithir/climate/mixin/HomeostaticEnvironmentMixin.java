package net.diaithir.climate.mixin;

import net.diaithir.climate.api.ClimateBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Indoors, Homeostatic's air temperature follows the room: its own outdoor figure plus how far the room is from outdoors. */
@Mixin(targets = "homeostatic.common.temperature.EnvironmentData", remap = false)
public abstract class HomeostaticEnvironmentMixin {
    @Shadow private float airTemperature;
    @Shadow private float localTemperature;
    @Shadow private boolean isSubmerged;
    @Shadow private boolean isPartialSubmersion;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void diaithirclimate$room(ServerPlayer player, BlockPos pos, Holder<Biome> biome, ServerLevel level, CallbackInfo ci) {
        double d = ClimateBridge.delta(level, player.blockPosition());
        if (d == 0.0) {
            return;
        }
        airTemperature += (float) d;
        if (!isSubmerged) {
            localTemperature += (float) (isPartialSubmersion ? 0.3 * d : d);
        }
    }
}
