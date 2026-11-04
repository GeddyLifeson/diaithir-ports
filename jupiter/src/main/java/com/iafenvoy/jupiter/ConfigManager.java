package com.iafenvoy.jupiter;

import com.iafenvoy.jupiter.compat.ExtraConfigManager;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager implements ResourceManagerReloadListener {
    private static final ConfigManager INSTANCE = new ConfigManager();

    private final Map<Identifier, AbstractConfigContainer> configHandlers = new ConcurrentHashMap<>();

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public void registerConfigHandler(Identifier id, AbstractConfigContainer container) {
        this.configHandlers.put(id, container);
        container.init();
        container.load();
    }

    public void registerConfigHandler(AbstractConfigContainer container) {
        this.registerConfigHandler(container.getConfigId(), container);
    }

    public void registerServerConfigHandler(AbstractConfigContainer container, ServerConfigManager.PermissionChecker checker) {
        this.registerConfigHandler(container);
        ServerConfigManager.registerServerConfig(container, checker);
    }

    public void registerServerConfig(AbstractConfigContainer container, ServerConfigManager.PermissionChecker checker) {
        ServerConfigManager.registerServerConfig(container, checker);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        ExtraConfigManager.scanConfigs();
        this.configHandlers.values().forEach(AbstractConfigContainer::load);
        Jupiter.LOGGER.info("Successfully reload {} common config(s).", this.configHandlers.size());
    }

    public Collection<AbstractConfigContainer> getConfigs() {
        return this.configHandlers.values();
    }
}
