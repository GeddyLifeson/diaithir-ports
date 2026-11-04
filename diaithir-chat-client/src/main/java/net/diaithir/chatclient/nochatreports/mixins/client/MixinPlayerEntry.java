// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: only the alwaysHideReportButton behaviour kept; the dedicated tooltip button is dropped.
package net.diaithir.chatclient.nochatreports.mixins.client;

import java.util.UUID;
import java.util.function.Supplier;
import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.diaithir.chatclient.nochatreports.client.SafetyState;
import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntry.class)
public class MixinPlayerEntry {
    @Shadow private Button reportButton;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ncr$onConstructed(Minecraft minecraft, SocialInteractionsScreen screen, UUID uuid, String name,
            Supplier<Identifier> skinGetter, boolean reportable, CallbackInfo info) {
        if (!NCRClient.enabled() || this.reportButton == null) {
            return;
        }
        if (NCRClientConfig.ALWAYS_HIDE_REPORT_BUTTON.get()
                || !SafetyState.areSigningKeysPresent() || !SafetyState.allowChatSigning()) {
            this.reportButton.active = false;
            this.reportButton.visible = false;
        }
    }
}
