package net.diaithir.inventoryui.trashslot;

import net.diaithir.inventoryui.trashslot.client.TrashClient;
import net.diaithir.inventoryui.trashslot.client.TrashClientConfig;
import net.diaithir.inventoryui.trashslot.common.TrashPayload;
import net.diaithir.inventoryui.trashslot.common.TrashServerConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, TrashServerConfig.SPEC, "diaithir_inventoryui-trashslot-server.toml");
        modBus.addListener(RegisterPayloadHandlersEvent.class, TrashPayload::register);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            container.registerConfig(ModConfig.Type.CLIENT, TrashClientConfig.SPEC, "diaithir_inventoryui-trashslot-client.toml");
            TrashClient.init(modBus);
        }
    }
}
