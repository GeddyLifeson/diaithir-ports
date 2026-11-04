package com.iafenvoy.jupiter.compat.forgeconfigspec;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.jupiter.config.ConfigSide;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.config.container.wrapper.ExtraConfigWrapper;
import com.iafenvoy.jupiter.internal.JupiterSettings;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigSpecLoader {
    private static int emptyEnumListCount = 0;

    public static void meetEmptyEnumList() {
        emptyEnumListCount++;
    }

    public static Map<String, EnumMap<ConfigSide, AbstractConfigContainer>> scanConfig() {
        Map<String, EnumMap<ConfigSide, AbstractConfigContainer>> data = new LinkedHashMap<>();
        if (!JupiterSettings.INSTANCE.general.loadForgeConfigs.getValue()) return data;
        Collection<ModConfig> configs = ModConfigs.getFileMap().values();
        emptyEnumListCount = 0;
        for (ModConfig config : configs) {
            try {
                if (!(config.getSpec() instanceof ModConfigSpec spec)) continue;
                IConfigSpec.ILoadedConfig valueHolder = config.getLoadedConfig();
                if (valueHolder == null) continue;
                ConfigSide type = switch (config.getType()) {
                    case COMMON -> ConfigSide.COMMON;
                    case CLIENT -> ConfigSide.CLIENT;
                    case SERVER -> ConfigSide.SERVER;
                    case STARTUP -> ConfigSide.STARTUP;
                };
                UnmodifiableConfig defaults = spec.getSpec();
                CommentedConfig values = valueHolder.config();
                Runnable saver = valueHolder::save;
                AbstractConfigContainer container = new ExtraConfigWrapper(new NightConfigHolder(config.getModId(), type, config.getFileName(), defaults, values, saver));
                ConfigManager.getInstance().registerConfigHandler(container);
                if (config.getType() != ModConfig.Type.CLIENT)
                    ServerConfigManager.registerServerConfig(container, ServerConfigManager.PermissionChecker.IS_OPERATOR, false);
                data.computeIfAbsent(config.getModId(), s -> new EnumMap<>(ConfigSide.class)).put(type, container);
            } catch (Exception e) {
                Jupiter.LOGGER.error("Failed to load config spec {}:{}:", config.getModId(), config.getType().extension(), e);
            }
        }
        if (emptyEnumListCount > 0)
            Jupiter.LOGGER.warn("Notice: Jupiter cannot resolve {} empty List<Enum> since technical issue in Java, it is recommended to add a value in default value list.", emptyEnumListCount);
        Jupiter.LOGGER.info("Config spec loading complete, found {} configs from {} mods.", data.values().stream().map(EnumMap::size).reduce(0, Integer::sum, Integer::sum), data.size());
        return data;
    }
}
