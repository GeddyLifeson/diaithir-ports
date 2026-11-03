package net.diaithir.gellings.client;

import net.diaithir.gellings.GellingMod;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = GellingMod.ID, value = Dist.CLIENT)
public final class GellingClient {
    private GellingClient() {}

    @SubscribeEvent
    static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        GellingMod.TYPES.forEach((id, t) -> {
            Identifier tex = Identifier.fromNamespaceAndPath(GellingMod.ID, "textures/entity/" + id + "_gelling.png");
            e.registerEntityRenderer(t.get(), ctx -> new SlimeRenderer(ctx) {
                @Override
                public Identifier getTextureLocation(SlimeRenderState state) { return tex; }
            });
        });
    }
}
