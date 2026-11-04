package com.diaithir.overworld_biomes.planets.mixin;

import com.diaithir.overworld_biomes.planets.PlanetGate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Loads the planet a player logged out in before their saved position is resolved, so they rejoin there and not in the Overworld. */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow
    @org.spongepowered.asm.mixin.Final
    private MinecraftServer server;

    @Inject(method = "loadPlayerData", at = @At("HEAD"))
    private void diaithir$preloadPlanet(NameAndId who, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        PlanetGate.preloadFor(this.server, ((PlayerList) (Object) this).getPlayerIo().load(who));
    }
}
