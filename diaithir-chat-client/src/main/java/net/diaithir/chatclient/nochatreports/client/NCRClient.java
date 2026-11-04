// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: platform event shims replaced by NeoForge client events; config/UI extras removed.
package net.diaithir.chatclient.nochatreports.client;

import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class NCRClient {
    private NCRClient() {}

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(new NCRClient.Handlers());
    }

    public static boolean enabled() {
        return NCRClientConfig.SPEC.isLoaded() && NCRClientConfig.ENABLE_MOD.get();
    }

    public static void onKeysResolved(boolean present) {
        SafetyState.setSigningKeysPresent(present);
    }

    public static final class Handlers {
        @SubscribeEvent
        public void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn e) {
            if (!enabled()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            ClientPacketListener handler = mc.getConnection();
            if (handler == null) {
                return;
            }
            mc.execute(() -> {
                SafetyState.setInSingleplayer(mc.isLocalServer());
                if (!mc.isLocalServer() && !SafetyState.isOnRealms()
                        && NCRClientConfig.DEFAULT_SIGNING_MODE.get() == NCRClientConfig.SigningMode.ALWAYS
                        && SafetyState.areSigningKeysPresent()) {
                    SafetyState.setAllowChatSigning(true);
                }
                boolean secure = SafetyState.isInSingleplayer()
                        || !handler.getConnection().isEncrypted()
                        || SafetyState.serverPreventsReports();
                if (NCRClientConfig.DEMAND_ON_SERVER.get() && !secure) {
                    handler.getConnection().disconnect(Component.translatable("disconnect.nochatreports.client"));
                }
            });
        }

        @SubscribeEvent
        public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut e) {
            if (enabled()) {
                SafetyState.reset();
            }
        }
    }
}
