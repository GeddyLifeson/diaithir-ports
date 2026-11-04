// Ported from Talk Balloons (LGPL-3.0-only) https://github.com/CERBON-MODS/Talk-Balloons
// Modifications: ServerChatEvent broadcasts the balloon payload to supporting clients via PacketDistributor
package net.diaithir.hudoverlays.talkballoons.server;

import net.diaithir.hudoverlays.talkballoons.config.TalkBalloonsConfig;
import net.diaithir.hudoverlays.talkballoons.network.BalloonPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TalkBalloonsServer {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ServerChatEvent event) {
        if (!TalkBalloonsConfig.ENABLED.get()) {
            return;
        }
        ServerPlayer player = event.getPlayer();
        Component text = Component.literal(event.getRawText());
        PacketDistributor.sendToAllPlayers(new BalloonPayload(player.getUUID(), text, TalkBalloonsConfig.BALLOON_AGE.get() * 20));
    }
}
