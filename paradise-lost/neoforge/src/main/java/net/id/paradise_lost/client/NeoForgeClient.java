package net.id.paradise_lost.client;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.item.XpCircletChargedProperty;
import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.id.paradise_lost.client.rendering.armor.OrnateOlviteArmorClientExtensions;
import net.id.paradise_lost.client.rendering.block.PalaceDoorBlockEntityRendererNeoForge;
import net.id.paradise_lost.client.rendering.particle.*;
import net.id.paradise_lost.client.rendering.ui.BloodstoneHUDRenderer;
import net.id.paradise_lost.client.rendering.util.ParadiseLostColorProviders;
import net.id.paradise_lost.client.screen.MoaScreen;
import net.id.paradise_lost.particle.ParadiseLostParticleTypes;
import net.id.paradise_lost.registry.ItemRegistry;
import net.id.paradise_lost.screen.ParadiseLostScreens;
import net.id.paradise_lost.util.ParadiseLostSoundEvents;
import net.id.paradise_lost.world.dimension.ParadiseLostDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClient {
    public NeoForgeClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, NeoForgeClient::registerRenderers);
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, NeoForgeClient::registerLayers);
        modBus.addListener(RegisterParticleProvidersEvent.class, NeoForgeClient::registerParticles);
        modBus.addListener(RegisterMenuScreensEvent.class, NeoForgeClient::registerScreens);
        modBus.addListener(RegisterColorHandlersEvent.BlockTintSources.class, NeoForgeClient::registerBlockTints);
        modBus.addListener(RegisterConditionalItemModelPropertyEvent.class, NeoForgeClient::registerItemModelProperties);
        modBus.addListener(RegisterClientExtensionsEvent.class, NeoForgeClient::registerClientExtensions);
        modBus.addListener(RegisterGuiLayersEvent.class, NeoForgeClient::registerGuiLayers);

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, NeoForgeClient::onSelectMusic);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new OrnateOlviteArmorClientExtensions(), ItemRegistry.OLVITE_HELMET_ORNATE.get());
        event.registerItem(new OlviteSpyglassClientExtensions(), ItemRegistry.OLVITE_SPYGLASS.get());
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ModConstants.locate("bloodstone_hud"), BloodstoneHUDRenderer::render);
    }

    private static void onSelectMusic(SelectMusicEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.level().dimension() == ParadiseLostDimension.PARADISE_LOST_WORLD_KEY) {
            event.setMusic(ParadiseLostSoundEvents.PARADISE_MUSIC_SOUND);
        }
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClientRegistration.registerRenderers(event::registerEntityRenderer, event::registerBlockEntityRenderer,
                PalaceDoorBlockEntityRendererNeoForge::new);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ParadiseLostModelLayers.ENTRIES.forEach((layer, definition) ->
                event.registerLayerDefinition(layer, () -> definition));
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParadiseLostParticleTypes.CHERINE_FLAME, CherineFlameParticle.DefaultFactory::new);
        event.registerSpriteSet(ParadiseLostParticleTypes.MOTHER_AUREL_LEAF, MotherAurelLeafParticle.DefaultFactory::new);
        event.registerSpriteSet(ParadiseLostParticleTypes.LEVITA_BLOOP, LevitaBloopParticle.DefaultFactory::new);
        event.registerSpriteSet(ParadiseLostParticleTypes.LEVITATION_TOTEM, LevitationTotemParticle.DefaultFactory::new);
        event.registerSpriteSet(ParadiseLostParticleTypes.LIT_CLOUD, LitCloudParticle.DefaultFactory::new);
        event.registerSpriteSet(ParadiseLostParticleTypes.LEVITA_SPARKLE, LevitaSparkleParticle.DefaultFactory::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ParadiseLostScreens.MOA.get(), MoaScreen::new);
    }

    private static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        ParadiseLostColorProviders.registerBlocks((sources, blocks) -> event.register(sources, blocks));
    }

    private static void registerItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(ModConstants.locate("charged"), XpCircletChargedProperty.MAP_CODEC);
    }
}
