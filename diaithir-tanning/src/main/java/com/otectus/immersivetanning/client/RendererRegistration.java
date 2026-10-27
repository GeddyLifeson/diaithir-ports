package com.otectus.immersivetanning.client;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.client.renderer.LeatherworkersBenchRenderer;
import com.otectus.immersivetanning.client.renderer.TannersRackRenderer;
import com.otectus.immersivetanning.client.renderer.TannersVatRenderer;
import com.otectus.immersivetanning.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only registration of the station block entity renderers. */
@EventBusSubscriber(modid = ImmersiveTanning.MOD_ID, value = Dist.CLIENT)
public final class RendererRegistration {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TANNERS_RACK.get(), TannersRackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TANNERS_VAT.get(), TannersVatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEATHERWORKERS_BENCH.get(), LeatherworkersBenchRenderer::new);
    }

    private RendererRegistration() {}
}
