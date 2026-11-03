package com.diaithir.overworld_biomes.planets.mixin;

import com.diaithir.overworld_biomes.planets.PlanetWeather;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.WeatherData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Planets keep their own weather state instead of the server-wide one. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "getWeatherData", at = @At("HEAD"), cancellable = true)
    private void diaithir$planetWeather(CallbackInfoReturnable<WeatherData> cir) {
        WeatherData w = PlanetWeather.of((ServerLevel) (Object) this);
        if (w != null) cir.setReturnValue(w);
    }
}
