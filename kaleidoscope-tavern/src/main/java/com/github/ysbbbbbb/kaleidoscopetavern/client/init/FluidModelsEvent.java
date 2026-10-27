package com.github.ysbbbbbb.kaleidoscopetavern.client.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;

import static com.github.ysbbbbbb.kaleidoscopetavern.init.ModFluids.*;

@EventBusSubscriber(value = Dist.CLIENT, modid = KaleidoscopeTavern.MOD_ID)
public class FluidModelsEvent {
    @SubscribeEvent
    public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        event.register(getFluidModel(GRAPE_JUICE_ID), GRAPE_JUICE.get());
        event.register(getFluidModel(GRAPE_JUICE_ID), FLOWING_GRAPE_JUICE.get());

        event.register(getFluidModel(ICE_GRAPE_JUICE_ID), ICE_GRAPE_JUICE.get());
        event.register(getFluidModel(ICE_GRAPE_JUICE_ID), FLOWING_ICE_GRAPE_JUICE.get());

        event.register(getFluidModel(GOLD_GRAPE_JUICE_ID), GOLD_GRAPE_JUICE.get());
        event.register(getFluidModel(GOLD_GRAPE_JUICE_ID), FLOWING_GOLD_GRAPE_JUICE.get());

        event.register(getFluidModel(GREEN_GRAPE_JUICE_ID), GREEN_GRAPE_JUICE.get());
        event.register(getFluidModel(GREEN_GRAPE_JUICE_ID), FLOWING_GREEN_GRAPE_JUICE.get());

        event.register(getFluidModel(SWEET_BERRIES_JUICE_ID), SWEET_BERRIES_JUICE.get());
        event.register(getFluidModel(SWEET_BERRIES_JUICE_ID), FLOWING_SWEET_BERRIES_JUICE.get());

        event.register(getFluidModel(GLOW_BERRIES_JUICE_ID), GLOW_BERRIES_JUICE.get());
        event.register(getFluidModel(GLOW_BERRIES_JUICE_ID), FLOWING_GLOW_BERRIES_JUICE.get());
    }

    public static FluidModel.Unbaked getFluidModel(Identifier id) {
        var stillTexture = Identifier.fromNamespaceAndPath(id.getNamespace(), "block/%s_still".formatted(id.getPath()));
        var flowingTexture = Identifier.fromNamespaceAndPath(id.getNamespace(), "block/%s_flow".formatted(id.getPath()));

        return new FluidModel.Unbaked(new Material(stillTexture), new Material(flowingTexture), null, null);
    }
}
