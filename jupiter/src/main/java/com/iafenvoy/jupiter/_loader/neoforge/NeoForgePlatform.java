package com.iafenvoy.jupiter._loader.neoforge;

import com.iafenvoy.jupiter.Platform;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

public final class NeoForgePlatform implements Platform {
    @Override
    public String resolveModName(String id) {
        return ModList.get().getModContainerById(id).map(ModContainer::getModInfo).map(IModInfo::getDisplayName)
                .orElse("%ERROR%");
    }

    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }
}
