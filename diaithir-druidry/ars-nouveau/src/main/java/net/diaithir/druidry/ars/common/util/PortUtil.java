package net.diaithir.druidry.ars.common.util;

import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketNoSpamChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PortUtil {
    public static void sendMessage(Entity playerEntity, Component component) {
        if (playerEntity == null)
            return;
        if (playerEntity instanceof net.minecraft.world.entity.player.Player player) player.sendSystemMessage(component);
    }

    public static void sendMessageNoSpam(Entity playerEntity, Component component) {
        if (playerEntity instanceof ServerPlayer serverPlayer) {
            Networking.sendToPlayerClient(new PacketNoSpamChatMessage(component, 0, false), serverPlayer);
        }
    }

    public static void sendMessageCenterScreen(Entity playerEntity, Component component) {
        if (playerEntity instanceof ServerPlayer serverPlayer) {
            Networking.sendToPlayerClient(new PacketNoSpamChatMessage(component, 0, true), serverPlayer);
        }
    }

    @Deprecated
    public static void sendMessage(Entity playerEntity, String message) {
        sendMessage(playerEntity, Component.literal(message));
    }

}
