package com.iafenvoy.jupiter._loader.fabric.compat;

import com.google.common.collect.ImmutableMap;
import com.iafenvoy.jupiter.compat.ExtraConfigManager;
import com.iafenvoy.jupiter.render.internal.JupiterConfigListScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.Map;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return JupiterConfigListScreen::new;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        ImmutableMap.Builder<String, ConfigScreenFactory<?>> builder = ImmutableMap.builder();
        for (String id : ExtraConfigManager.getProvidedMods()) builder.put(id, ExtraConfigManager.getScreen(id)::apply);
        return builder.build();
    }
}