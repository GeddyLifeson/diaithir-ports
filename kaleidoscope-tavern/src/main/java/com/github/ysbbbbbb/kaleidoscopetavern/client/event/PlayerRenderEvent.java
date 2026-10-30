package com.github.ysbbbbbb.kaleidoscopetavern.client.event;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = KaleidoscopeTavern.MOD_ID, value = Dist.CLIENT)
public class PlayerRenderEvent {
    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre<?> event) {
        Boolean renderData = event.getRenderState().getRenderData(RenderStateEvent.GRASS_STEALTH_HIDE);
        if (Boolean.TRUE.equals(renderData)) {
            event.setCanceled(true);
        }
    }
}