// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: ChatListener/ClientPacketListener mixins replaced by a ClientChatReceivedEvent handler (sender UUID from the event).
package net.diaithir.chatclient.chatheads.client;

import com.mojang.logging.LogUtils;
import java.util.UUID;
import net.diaithir.chatclient.chatheads.config.ChatHeadsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

public final class ChatHeadsClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ChatHeadsClient() {}

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(new Handlers());
    }

    public static final class Handlers {
        // lowest priority: run after other mods (e.g. timestamps) so heads end up in the right place
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onChat(ClientChatReceivedEvent e) {
            if (e instanceof ClientChatReceivedEvent.System sys) {
                // action bar messages never get heads
                if (sys.isOverlay() || !ChatHeadsConfig.HANDLE_SYSTEM_MESSAGES.get()) {
                    return;
                }
            }

            try {
                PlayerInfo info = null;
                UUID sender = e.getSender();
                var connection = Minecraft.getInstance().getConnection();
                if (connection != null && sender != null) {
                    // returns null for the nil UUID used by system/disguised messages
                    info = connection.getPlayerInfo(sender);
                }
                e.setMessage(HeadLogic.handleAddedMessage(e.getMessage(), info));
            } catch (RuntimeException ex) {
                LOGGER.warn("Chat Heads failed to process a chat message", ex);
            }
        }

        @SubscribeEvent
        public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
            HeadLogic.resetServerKnowledge();
        }
    }
}
