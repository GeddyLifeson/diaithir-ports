package com.github.ysbbbbbb.kaleidoscopetavern.init.register;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = KaleidoscopeTavern.MOD_ID)
public class DatapackSyncEvent {
    @SubscribeEvent
    public static void onDatapackSyncEvent(OnDatapackSyncEvent event) {
        event.sendRecipes(
                ModRecipes.BARREL_RECIPE.get(),
                ModRecipes.PRESSING_TUB_RECIPE.get()
        );
    }
}
