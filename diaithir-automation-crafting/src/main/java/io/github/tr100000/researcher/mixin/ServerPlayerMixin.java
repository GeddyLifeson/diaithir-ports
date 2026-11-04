package io.github.tr100000.researcher.mixin;

import com.mojang.authlib.GameProfile;
import io.github.tr100000.researcher.PlayerResearchTracker;
import io.github.tr100000.researcher.api.PlayerResearchTrackerGetter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerResearchTrackerGetter {
    @Unique
    private PlayerResearchTracker researchTracker;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftServer server, ServerLevel level, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
        researchTracker = server.getPlayerList().researcher$getPlayerTracker((ServerPlayer)(Object)this);
    }

    @Inject(method = "tick", at = @At(("TAIL")))
    private void tick(CallbackInfo ci) {
        researchTracker.sendUpdate();
    }

    @Override @Unique
    public PlayerResearchTracker researcher$getPlayerTracker() {
        return researchTracker;
    }
}
