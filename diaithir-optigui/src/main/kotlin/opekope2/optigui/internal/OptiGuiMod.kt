package opekope2.optigui.internal

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import opekope2.optigui.internal.interaction.InteractionHandler
import opekope2.optigui.internal.selector.registerLoadSelectors
import opekope2.optigui.internal.selector.registerSelectors
import opekope2.optigui.registry.ContainerDefaultGuiTextureRegistry
import opekope2.optigui.util.MOD_ID

/**
 * NeoForge entry point. Replaces the Fabric `client` entrypoints of upstream OptiGUI; everything else is unchanged.
 */
@Mod(value = MOD_ID, dist = [Dist.CLIENT])
class OptiGuiMod(modBus: IEventBus) {
    init {
        initialize()
        registerSelectors()
        registerLoadSelectors()

        modBus.addListener(RegisterKeyMappingsEvent::class.java) {
            it.registerCategory(OPTIGUI_INSPECTOR_CATEGORY)
            it.register(INSPECTOR_KEY_BINDING)
        }
        modBus.addListener(AddClientReloadListenersEvent::class.java) {
            it.addListener(FilterLoader.id, FilterLoader)
        }
        modBus.addListener(FMLClientSetupEvent::class.java) {
            ContainerDefaultGuiTextureRegistry.onInitializeClient()
        }

        TextureReplacer.onInitializeClient()
        InteractionHandler.onInitializeClient()
    }
}
