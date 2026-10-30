package com.github.ysbbbbbb.kaleidoscopetavern.client.init;


import com.github.ysbbbbbb.kaleidoscopetavern.client.model.brew.BarrelModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.model.deco.BarStoolBodyModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.model.deco.LargeChalkboardModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.model.deco.SmallChalkboardModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.entity.SitRenderer;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.entity.ThrownMolotovRenderer;
import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.entity.ThrownMolotovEntity;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ModEntitiesRender {
    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        EntityRenderers.register(SitEntity.TYPE, SitRenderer::new);
        EntityRenderers.register(ThrownMolotovEntity.TYPE, ThrownMolotovRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SmallChalkboardModel.LAYER_LOCATION, SmallChalkboardModel::createBodyLayer);
        event.registerLayerDefinition(LargeChalkboardModel.LAYER_LOCATION, LargeChalkboardModel::createBodyLayer);
        event.registerLayerDefinition(BarStoolBodyModel.LAYER_LOCATION, BarStoolBodyModel::createBodyLayer);
        event.registerLayerDefinition(BarrelModel.LAYER_LOCATION, BarrelModel::createBodyLayer);
    }
}
