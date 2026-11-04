package net.diaithir.druidry.ars.common.entity.pathfinding;

import net.diaithir.druidry.ars.client.ClientInfo;
import net.diaithir.druidry.ars.common.light.LightManager;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Used to handle client events.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void clientPlayerLogin(ClientPlayerNetworkEvent.LoggingIn e) {
        if(e.getPlayer() != null){
            if(Config.INFORM_LIGHTS.get()){
                Player entity = e.getPlayer();
                PortUtil.sendMessage(entity, Component.translatable("ars_nouveau.light_message").withStyle(ChatFormatting.GOLD));
                Config.INFORM_LIGHTS.set(false);
                Config.INFORM_LIGHTS.save();
            }
        }
    }
}
