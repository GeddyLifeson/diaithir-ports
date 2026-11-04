// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Single feature entry point; config screen, GameStages/CraftTweaker compat dropped, registration wired from the shared mod bus.
package net.diaithir.structurenavigation.structurecompass;

import net.diaithir.structurenavigation.structurecompass.client.CompassClient;
import net.diaithir.structurenavigation.structurecompass.config.CompassConfig;
import net.diaithir.structurenavigation.structurecompass.network.CompassNetwork;
import net.diaithir.structurenavigation.structurecompass.registry.CompassComponents;
import net.diaithir.structurenavigation.structurecompass.registry.CompassItems;
import net.diaithir.structurenavigation.structurecompass.util.AsyncLocator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CompassConfig.SPEC, "diaithir_structurenavigation_structurecompass-common.toml");

        CompassComponents.DATA_COMPONENT_TYPES.register(modBus);
        CompassItems.ITEMS.register(modBus);
        CompassItems.CREATIVE_MODE_TABS.register(modBus);

        modBus.addListener(RegisterPayloadHandlersEvent.class, CompassNetwork::register);

        NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class, e -> AsyncLocator.handleServerAboutToStartEvent());
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, e -> AsyncLocator.handleServerStoppingEvent());

        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        CompassClient.init(modBus);
    }
}
