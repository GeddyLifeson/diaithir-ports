package com.diaithir.overworldbiomes.compat;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only registration for the compat boats and sign wood types. */
final class CompatClient {
    private CompatClient() {}

    private static ModelLayerLocation layer(String ns, String w, boolean chest) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ns, (chest ? "chest_boat/" : "boat/") + w), "main");
    }

    static void init(IEventBus bus) {
        bus.addListener((EntityRenderersEvent.RegisterLayerDefinitions e) -> {
            for (Object[] b : CompatBlocks.BOATS) {
                boolean chest = (Boolean) b[2];
                e.registerLayerDefinition(layer((String) b[0], (String) b[1], chest), chest ? BoatModel::createChestBoatModel : BoatModel::createBoatModel);
            }
        });
        bus.addListener((EntityRenderersEvent.RegisterRenderers e) -> {
            for (Object[] b : CompatBlocks.BOATS) {
                boolean chest = (Boolean) b[2];
                ModelLayerLocation loc = layer((String) b[0], (String) b[1], chest);
                @SuppressWarnings("unchecked")
                EntityType<net.minecraft.world.entity.vehicle.boat.AbstractBoat> t = (EntityType<net.minecraft.world.entity.vehicle.boat.AbstractBoat>) ((net.neoforged.neoforge.registries.DeferredHolder<?, ?>) b[3]).get();
                e.registerEntityRenderer(t, ctx -> new BoatRenderer(ctx, loc));
            }
        });
        bus.addListener((FMLClientSetupEvent e) -> e.enqueueWork(() -> {
            for (WoodType wt : CompatBlocks.WOODS.values()) Sheets.addWoodType(wt);
        }));
    }
}
