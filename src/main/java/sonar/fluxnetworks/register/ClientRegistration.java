package sonar.fluxnetworks.register;

import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.client.FluxColorHandler;
import sonar.fluxnetworks.client.gui.GuiFluxAdminHome;
import sonar.fluxnetworks.client.gui.GuiFluxDeviceHome;
import sonar.fluxnetworks.client.render.FluxStorageEntityRenderer;
import sonar.fluxnetworks.common.connection.FluxMenu;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FluxNetworks.MODID)
public class ClientRegistration {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        Channel.get().setS2CMessageHandler((index, payload, context) -> ClientMessages.msg(
                index, payload, () -> (LocalPlayer) context.player(),
                Minecraft.getInstance()
        ));
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(RegistryMenuTypes.FLUX_MENU.get(),
                getScreenFactory());
    }

    @Nonnull
    private static MenuScreens.ScreenConstructor<FluxMenu, AbstractContainerScreen<FluxMenu>> getScreenFactory() {
        return (menu, inventory, title) -> {
            if (menu.mProvider instanceof TileFluxDevice) {
                return new GuiFluxDeviceHome(menu, inventory.player);
            }
            /*if (menu.bridge instanceof ItemFluxConfigurator.MenuBridge) {
                return new GuiFluxConfiguratorHome(menu, inventory.player);
            }*/
            return new GuiFluxAdminHome(menu, inventory.player);
        };
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegistryBlockEntityTypes.BASIC_FLUX_STORAGE.get(),
                FluxStorageEntityRenderer.PROVIDER);
        event.registerBlockEntityRenderer(RegistryBlockEntityTypes.HERCULEAN_FLUX_STORAGE.get(),
                FluxStorageEntityRenderer.PROVIDER);
        event.registerBlockEntityRenderer(RegistryBlockEntityTypes.GARGANTUAN_FLUX_STORAGE.get(),
                FluxStorageEntityRenderer.PROVIDER);
    }

    @SubscribeEvent
    public static void registerItemTintSources(@Nonnull RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(FluxNetworks.location("network_color"), FluxColorHandler.NetworkTint.CODEC);
    }

    @SubscribeEvent
    public static void registerBlockTintSources(@Nonnull RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(FluxColorHandler.BLOCK_TINTS,
                RegistryBlocks.FLUX_CONTROLLER.get(),
                RegistryBlocks.FLUX_POINT.get(),
                RegistryBlocks.FLUX_PLUG.get(),
                RegistryBlocks.BASIC_FLUX_STORAGE.get(),
                RegistryBlocks.HERCULEAN_FLUX_STORAGE.get(),
                RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get());
    }
}
