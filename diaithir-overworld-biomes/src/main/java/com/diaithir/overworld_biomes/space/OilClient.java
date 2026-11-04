package com.diaithir.overworld_biomes.space;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;

/** Oil uses the water sprites until it gets its own art. */
final class OilClient {
    private OilClient() {}

    static void init(IEventBus bus) {
        bus.addListener((RegisterFluidModelsEvent e) -> e.register(
                new FluidModel.Unbaked(new Material(Identifier.withDefaultNamespace("block/water_still")), new Material(Identifier.withDefaultNamespace("block/water_flow")), null, null),
                OilFluid.SOURCE, OilFluid.FLOWING));
    }
}
