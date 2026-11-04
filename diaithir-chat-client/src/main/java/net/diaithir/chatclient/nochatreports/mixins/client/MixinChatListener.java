// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: signing prompt screen removed (PROMPT behaves like ON_DEMAND); trust indicator suppression kept.
package net.diaithir.chatclient.nochatreports.mixins.client;

import java.time.Instant;
import java.util.UUID;
import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.diaithir.chatclient.nochatreports.client.SafetyState;
import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatListener.class)
public class MixinChatListener {

    @Shadow
    private boolean isSenderLocalPlayer(UUID uuid) {
        throw new IllegalStateException("@Shadow transformation failed. Should never happen.");
    }

    @Shadow
    public void handleSystemMessage(Component message, boolean overlay) {
        throw new IllegalStateException("@Shadow transformation failed. Should never happen.");
    }

    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    private void ncr$onHandleSystemMessage(Component message, boolean overlay, CallbackInfo info) {
        if (!NCRClient.enabled()) {
            return;
        }
        if (message.getContents() instanceof TranslatableContents translatable
                && translatable.getKey().equals("chat.disabled.missingProfileKey")) {
            NCRClientConfig.SigningMode mode = NCRClientConfig.DEFAULT_SIGNING_MODE.get();
            boolean willEnable = !(SafetyState.allowChatSigning() || mode == NCRClientConfig.SigningMode.NEVER
                    || !SafetyState.areSigningKeysPresent());
            if (willEnable) {
                // PROMPT and ON_DEMAND: enable signing for this session so the next message goes through.
                SafetyState.setAllowChatSigning(true);
            }
            info.cancel();
            this.handleSystemMessage(Component.translatable("chat.nochatreports.disabled.signing_requested"), overlay);
        }
    }

    @Inject(method = "evaluateTrustLevel", at = @At("HEAD"), cancellable = true)
    private void ncr$onEvaluateTrustLevel(PlayerChatMessage playerChatMessage, Component component, Instant instant,
            CallbackInfoReturnable<ChatTrustLevel> info) {
        if (!NCRClientConfig.SPEC.isLoaded()) {
            return;
        }
        if (this.isSenderLocalPlayer(playerChatMessage.sender())) {
            info.setReturnValue(ChatTrustLevel.SECURE);
            return;
        }
        ChatTrustLevel evaluate = ChatTrustLevel.evaluate(playerChatMessage, component, instant);
        if (evaluate == ChatTrustLevel.NOT_SECURE && NCRClientConfig.HIDE_INSECURE_INDICATORS.get()) {
            info.setReturnValue(ChatTrustLevel.SECURE);
        } else if (evaluate == ChatTrustLevel.MODIFIED && NCRClientConfig.HIDE_MODIFIED_INDICATORS.get()) {
            info.setReturnValue(ChatTrustLevel.SECURE);
        }
    }
}
