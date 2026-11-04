// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: resets per-connection safety state; server address tracking dropped (no per-server prefs).
package net.diaithir.chatclient.nochatreports.mixins.client;

import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.diaithir.chatclient.nochatreports.client.SafetyState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {

    @Inject(method = "startConnecting", at = @At("HEAD"))
    private static void ncr$onStartConnecting(Screen screen, Minecraft minecraft, ServerAddress serverAddress,
            @Nullable ServerData serverData, boolean quickplay, @Nullable TransferState transferState, CallbackInfo info) {
        if (!NCRClient.enabled()) {
            return;
        }
        SafetyState.reset();
    }
}
