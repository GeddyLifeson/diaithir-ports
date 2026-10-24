package com.zurrtum.create.api.registry;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.platform.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface CreateRegisterPlugin {
    List<CreateRegisterPlugin> PLUGINS = createPlugins();

    default void onBlockRegister() {
    }

    default void onFluidRegister() {
    }

    default void onDataLoaderRegister() {
    }

    default void onEntityAttributeRegister() {
    }

    static List<CreateRegisterPlugin> createPlugins() {
        List<CreateRegisterPlugin> plugins = new ArrayList<>();
        plugins.add(new com.zurrtum.create.AllEarlyRegistries());
        java.util.ServiceLoader.load(CreateRegisterPlugin.class).forEach(plugins::add);
        return plugins;
    }

    static void registerBlock() {
        PLUGINS.forEach(CreateRegisterPlugin::onBlockRegister);
    }

    static void registerFluid() {
        PLUGINS.forEach(CreateRegisterPlugin::onFluidRegister);
    }

    static void registerDataLoader() {
        PLUGINS.forEach(CreateRegisterPlugin::onDataLoaderRegister);
    }

    static void registerEntityAttributes() {
        PLUGINS.forEach(CreateRegisterPlugin::onEntityAttributeRegister);
    }
}
