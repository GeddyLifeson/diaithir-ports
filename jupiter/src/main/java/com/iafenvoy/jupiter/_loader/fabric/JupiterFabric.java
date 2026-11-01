package com.iafenvoy.jupiter._loader.fabric;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.JupiterProxies;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.jupiter._loader.fabric.network.FabricClientNetworkHelper;
import com.iafenvoy.jupiter._loader.fabric.network.FabricServerNetworkHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public final class JupiterFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        JupiterProxies.PLATFORM = new FabricPlatform();
        JupiterProxies.CLIENT_NETWORKING = new FabricClientNetworkHelper();
        JupiterProxies.SERVER_NETWORKING = new FabricServerNetworkHelper();

        Jupiter.init(FabricLoader.getInstance().isDevelopmentEnvironment());
        Jupiter.process();
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "server_config_reload"), new ServerConfigManager());
    }
}
