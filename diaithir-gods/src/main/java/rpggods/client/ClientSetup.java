package rpggods.client;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import rpggods.RGRegistry;
import rpggods.client.blockentity.BrazierBlockEntityRenderer;
import rpggods.client.entity.AltarArmorModel;
import rpggods.client.entity.AltarModel;
import rpggods.client.entity.AltarRenderer;
import rpggods.client.screen.AltarScreen;
import rpggods.client.screen.FavorScreen;

/** Registers screens, renderers and model layers on the client **/
public final class ClientSetup {
    private ClientSetup() {}

    public static void init(final IEventBus modBus) {
        modBus.addListener(ClientSetup::registerScreens);
        modBus.addListener(ClientSetup::registerRenderers);
        modBus.addListener(ClientSetup::registerLayers);
    }

    private static void registerScreens(final RegisterMenuScreensEvent event) {
        event.register(RGRegistry.ALTAR_CONTAINER.get(), AltarScreen::new);
        event.register(RGRegistry.FAVOR_CONTAINER.get(), FavorScreen::new);
    }

    private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RGRegistry.ALTAR_TYPE.get(), AltarRenderer::new);
        event.registerBlockEntityRenderer(RGRegistry.BRAZIER_TYPE.get(), BrazierBlockEntityRenderer::new);
    }

    private static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AltarRenderer.ALTAR_MODEL_RESOURCE, AltarModel::createBodyLayer);
        registerArmor(event, AltarRenderer.ALTAR_INNER_ARMOR_RESOURCE, AltarArmorModel.createArmorLayerSet(new CubeDeformation(0.5F), new CubeDeformation(1.0F)));
        registerArmor(event, AltarRenderer.ALTAR_OUTER_ARMOR_RESOURCE, AltarArmorModel.createArmorLayerSet(new CubeDeformation(0.5F), new CubeDeformation(1.0F)));
    }

    private static void registerArmor(final EntityRenderersEvent.RegisterLayerDefinitions event,
                                      final ArmorModelSet<net.minecraft.client.model.geom.ModelLayerLocation> locations,
                                      final ArmorModelSet<net.minecraft.client.model.geom.builders.LayerDefinition> definitions) {
        event.registerLayerDefinition(locations.head(), () -> definitions.head());
        event.registerLayerDefinition(locations.chest(), () -> definitions.chest());
        event.registerLayerDefinition(locations.legs(), () -> definitions.legs());
        event.registerLayerDefinition(locations.feet(), () -> definitions.feet());
    }
}
