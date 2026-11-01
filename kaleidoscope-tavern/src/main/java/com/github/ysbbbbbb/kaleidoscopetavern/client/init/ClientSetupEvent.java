package com.github.ysbbbbbb.kaleidoscopetavern.client.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.*;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModFluids;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = KaleidoscopeTavern.MOD_ID)
public class ClientSetupEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 其他模组兼容
        // PonderCompat.init();
    }

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRenderers.register(ModBlocks.CHALKBOARD_BE.get(), ChalkboardBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlocks.SANDWICH_BOARD_BE.get(), SandwichBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlocks.PRESSING_TUB_BE.get(), PressingTubBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlocks.BARREL_BE.get(), BarrelBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlocks.BAR_CABINET_BE.get(), BarCabinetBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlocks.BAR_STOOL_BE.get(), BarStoolBlockEntityRender::new);
    }
}
