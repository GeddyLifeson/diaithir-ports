package com.diaithir.nether.incendium;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Game-bus (NeoForge bus) event handlers for Incendium.
 * LivingIncomingDamageEvent is NOT a mod-bus event, so this subscriber must
 * live on the default game bus.
 */
@EventBusSubscriber(modid = IncendiumRegistries.NS)
public final class IncendiumEvents {
    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent event) {
        // Intentionally left as a safe no-op hook; damage modifiers are applied via item/attribute logic.
    }

    private IncendiumEvents() {}
}
