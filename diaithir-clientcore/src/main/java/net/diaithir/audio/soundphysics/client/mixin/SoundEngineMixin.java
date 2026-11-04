// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: merged SoundSystemMixin/ShadowInstanceToChannelMixin hooks; play and tick moved to NeoForge events; wrapper map moved out of the mixin.
package net.diaithir.audio.soundphysics.client.mixin;

import java.util.Map;
import net.diaithir.audio.soundphysics.client.InstanceToChannelMap;
import net.diaithir.audio.soundphysics.client.SoundPhysicsClient;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SoundEngine.class, priority = 9001)
public abstract class SoundEngineMixin {
    @Shadow
    @Mutable
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void spp$init(CallbackInfo ci) {
        this.instanceToChannel = new InstanceToChannelMap(this.instanceToChannel);
        SoundPhysicsClient.setEngine((SoundEngine) (Object) this);
    }

    @Inject(method = "loadLibrary()V", at = @At("TAIL"))
    private void spp$onLoadLibrary(CallbackInfo ci) {
        SoundPhysicsClient.setEngine((SoundEngine) (Object) this);
        SoundPhysicsClient.onAudioEngineStart();
    }

    @Inject(method = "stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"))
    private void spp$onStop(SoundInstance sound, CallbackInfo ci) {
        SoundPhysicsClient.onStop(sound, this.instanceToChannel);
    }

    @Inject(method = "stopAll()V", at = @At("HEAD"))
    private void spp$onStopAll(CallbackInfo ci) {
        SoundPhysicsClient.onStopAll();
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    private void spp$onDestroy(CallbackInfo ci) {
        SoundPhysicsClient.onEngineShutdown("destroy()");
    }

    @Inject(method = "emergencyShutdown()V", at = @At("HEAD"))
    private void spp$onEmergencyShutdown(CallbackInfo ci) {
        SoundPhysicsClient.onEngineShutdown("emergencyShutdown()");
    }
}
