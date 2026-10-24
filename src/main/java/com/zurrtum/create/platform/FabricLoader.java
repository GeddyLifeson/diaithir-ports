package com.zurrtum.create.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Minimal loader facade so code ported from the Fabric version keeps its call sites.
 */
public final class FabricLoader {
    private static final FabricLoader INSTANCE = new FabricLoader();

    private FabricLoader() {
    }

    public static FabricLoader getInstance() {
        return INSTANCE;
    }

    public boolean isModLoaded(String id) {
        ModList list = ModList.get();
        if (list == null) {
            return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
        }
        return list.isLoaded(id);
    }

    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    public Optional<ModContainer> getModContainer(String id) {
        if (!isModLoaded(id)) {
            return Optional.empty();
        }
        return Optional.of(new ModContainer(id));
    }

    public Collection<ModContainer> getAllMods() {
        ModList list = ModList.get();
        if (list == null) {
            return List.of();
        }
        return list.getMods().stream().map(info -> new ModContainer(info.getModId())).toList();
    }
}
