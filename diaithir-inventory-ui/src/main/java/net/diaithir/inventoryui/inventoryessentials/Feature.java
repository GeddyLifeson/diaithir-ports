package net.diaithir.inventoryui.inventoryessentials;

import net.diaithir.inventoryui.inventoryessentials.config.ClientConfig;
import net.diaithir.inventoryui.inventoryessentials.config.ServerConfig;
import net.diaithir.inventoryui.inventoryessentials.network.ServerNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ServerConfig.SPEC, "inventoryui-inventoryessentials-common.toml");
        modBus.addListener(ServerNetwork::register);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "inventoryui-inventoryessentials-client.toml");
            net.diaithir.inventoryui.inventoryessentials.client.ClientSetup.init(modBus, container);
        }
    }
}
