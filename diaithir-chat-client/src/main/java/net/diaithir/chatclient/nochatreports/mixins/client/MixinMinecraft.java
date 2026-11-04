// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: uses SafetyState/NCRClientConfig of this feature; no mixin refmap dependency on platform shims.
package net.diaithir.chatclient.nochatreports.mixins.client;

import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.diaithir.chatclient.nochatreports.client.SafetyState;
import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow @Final
    private ProfileKeyPairManager profileKeyPairManager;

    @Inject(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At("RETURN"))
    private void ncr$onConstructed(GameConfig config, CallbackInfo info) {
        NCRClient.onKeysResolved(this.profileKeyPairManager.prepareKeyPair().join().isPresent());
    }

    @Inject(method = "getProfileKeyPairManager", at = @At("HEAD"), cancellable = true)
    private void ncr$onGetProfileKeyPairManager(CallbackInfoReturnable<ProfileKeyPairManager> info) {
        if (!NCRClient.enabled()) {
            return;
        }
        if (!SafetyState.allowChatSigning()) {
            info.setReturnValue(ProfileKeyPairManager.EMPTY_KEY_MANAGER);
        }
    }

    @Inject(method = "allowsTelemetry", at = @At("HEAD"), cancellable = true)
    private void ncr$onAllowsTelemetry(CallbackInfoReturnable<Boolean> info) {
        if (NCRClientConfig.SPEC.isLoaded() && NCRClientConfig.DISABLE_TELEMETRY.get()) {
            info.setReturnValue(false);
        }
    }
}
