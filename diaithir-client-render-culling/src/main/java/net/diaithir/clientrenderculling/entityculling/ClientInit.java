package net.diaithir.clientrenderculling.entityculling;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

final class ClientInit {
    private ClientInit() {}

    static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, CullConfig.SPEC, "diaithir-entity-culling-client.toml");
        modBus.addListener(CullConfig::onReload);
        modBus.addListener((RegisterKeyMappingsEvent e) -> {
            e.register(CullKeys.TOGGLE);
            e.register(CullKeys.DEBUG);
        });
        NeoForge.EVENT_BUS.register(new CullEvents());
    }
}
