package com.misterd.agritechevolved;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.blockentity.custom.*;
import com.misterd.agritechevolved.client.ber.AdvancedPlanterBlockEntityRenderer;
import com.misterd.agritechevolved.client.ber.PlanterBlockEntityRenderer;
import com.misterd.agritechevolved.component.ATEDataComponents;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.gui.ATEMenuTypes;
import com.misterd.agritechevolved.gui.custom.*;
import com.misterd.agritechevolved.item.ATECreativeTab;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.recipe.ATERecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(AgritechEvolved.MODID)
public class AgritechEvolved {
    public static final String MODID = "agritechevolved";
    public static int RECIPE_REVISION = 0;
    public static final Logger LOGGER = LogUtils.getLogger();

    public AgritechEvolved(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        ATEBlocks.register(modEventBus);
        ATEBlockEntities.register(modEventBus);
        ATEItems.register(modEventBus);
        ATECreativeTab.register(modEventBus);
        ATEMenuTypes.register(modEventBus);
        ATEDataComponents.register(modEventBus);
        ATERecipe.register(modEventBus);
        ATEDataMaps.register(modEventBus);

        modEventBus.addListener(AdvancedPlanterBlockEntity::registerCapabilities);
        modEventBus.addListener(ComposterBlockEntity::registerCapabilities);
        modEventBus.addListener(BiomassBurnerBlockEntity::registerCapabilities);
        modEventBus.addListener(CapacitorBlockEntity::registerCapabilities);
        modEventBus.addListener(SiloBlockEntity::registerCapabilities);
        modEventBus.addListener(FertilizerSpreaderBlockEntity::registerCapabilities);

        Config.register(modContainer);
        modEventBus.register(Config.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @SubscribeEvent
    public void onServerReload(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MODID, "recipe_revision_tracker"),
                new SimplePreparableReloadListener<Void>() {
                    @Override
                    protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
                        return null;
                    }
                    @Override
                    protected void apply(Void object, ResourceManager manager, ProfilerFiller profiler) {
                        RECIPE_REVISION++;
                    }
                });
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {

        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ATEBlockEntities.PLANTER_BLOCK_BE.get(), PlanterBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ATEBlockEntities.ADVANCED_PLANTER_BLOCK_BE.get(), AdvancedPlanterBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ATEMenuTypes.PLANTER_BLOCK_MENU.get(), BasicPlanterScreen::new);
            event.register(ATEMenuTypes.ADVANCED_PLANTER_BLOCK_MENU.get(), AdvancedPlanterScreen::new);
            event.register(ATEMenuTypes.COMPOSTER_MENU.get(), ComposterScreen::new);
            event.register(ATEMenuTypes.BURNER_MENU.get(), BiomassBurnerScreen::new);
            event.register(ATEMenuTypes.CAPACITOR_MENU.get(), CapacitorScreen::new);
            event.register(ATEMenuTypes.FERTILIZER_SPREADER_MENU.get(), FertilizerSpreaderScreen::new);
            event.register(ATEMenuTypes.SILO_MENU.get(), SiloScreen::new);
        }

        @SubscribeEvent
        public static void onRegisterStandaloneModels(ModelEvent.RegisterStandalone event) {
            PlanterBlockEntityRenderer.onRegisterStandaloneModels(event);
        }
    }
}