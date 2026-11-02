package net.diaithir.clientrenderculling.entityculling;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class CullEvents {
    static final TagKey<EntityType<?>> NEVER_CULL =
        TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("diaithir_client_render_culling", "never_cull"));

    @SubscribeEvent
    public void onTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        while (CullKeys.TOGGLE.consumeClick()) {
            CullEngine.enabled = !CullEngine.enabled;
            if (!CullEngine.enabled) CullEngine.CACHE.clear();
        }
        while (CullKeys.DEBUG.consumeClick()) CullConfig.DEBUG.set(!CullConfig.DEBUG.get());
        CullEngine.tick(mc);
    }

    @SubscribeEvent
    public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        CullEngine.shutdown();
    }

    @SubscribeEvent
    public void onUnload(LevelEvent.Unload e) {
        if (e.getLevel().isClientSide()) CullEngine.CACHE.clear();
    }
}

// Note: Entity/debug-overlay rendering hooks were removed because the corresponding APIs changed
// (GuiGraphics -> GuiGraphicsExtractor, RenderLivingEvent render state no longer exposes entity id).
// Cached results for block entities and entities are published in CullEngine.CACHE
// (keys: Entity / immutable BlockPos); a mixin calling CullEngine.isHidden(key) could apply culling.
