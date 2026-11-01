package com.iafenvoy.jupiter._loader.fabric;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.Jupiter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public final class JupiterFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Jupiter.processClient();
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "client_config_reload"), new ConfigManager());
    }
}
