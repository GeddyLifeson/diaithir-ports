package com.iafenvoy.jupiter._loader.fabric;

import com.iafenvoy.jupiter.Platform;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public final class FabricPlatform implements Platform {
    @Override
    public String resolveModName(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("%ERROR%");
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
