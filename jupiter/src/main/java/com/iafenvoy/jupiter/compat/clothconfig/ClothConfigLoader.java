package com.iafenvoy.jupiter.compat.clothconfig;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.jupiter.config.ConfigSide;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.config.container.wrapper.ExtraConfigWrapper;
import com.iafenvoy.jupiter.internal.JupiterSettings;
import com.iafenvoy.jupiter.mixin.AutoConfigAccessor;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClothConfigLoader {
    private static final Map<Class<?>, AbstractConfigContainer> BY_CLASS = new LinkedHashMap<>();

    @SuppressWarnings({"rawtypes", "UnstableApiUsage"})
    public static Map<String, EnumMap<ConfigSide, AbstractConfigContainer>> scanConfig() {
        BY_CLASS.clear();
        Map<String, EnumMap<ConfigSide, AbstractConfigContainer>> data = new LinkedHashMap<>();
        if (!JupiterSettings.INSTANCE.general.loadClothConfigs.getValue()) return data;
        List<me.shedaniel.autoconfig.ConfigManager> holders = AutoConfigAccessor.getAllConfigs().values().stream().map(me.shedaniel.autoconfig.ConfigManager.class::cast).toList();
        for (me.shedaniel.autoconfig.ConfigManager<?> manager : holders) {
            ClothConfigHolder<?> holder = new ClothConfigHolder<>(manager);
            AbstractConfigContainer container = new ExtraConfigWrapper(holder);
            data.put(holder.getModId(), Util.make(new EnumMap<>(ConfigSide.class), m -> m.put(ConfigSide.UNKNOWN, container)));
            ConfigManager.getInstance().registerServerConfigHandler(container, ServerConfigManager.PermissionChecker.IS_OPERATOR);
            BY_CLASS.put(manager.getConfigClass(), container);
        }
        Jupiter.LOGGER.info("Cloth Config loading complete, found {} configs from {} mods.", data.values().stream().map(EnumMap::size).reduce(0, Integer::sum, Integer::sum), data.size());
        return data;
    }

    @Nullable
    public static AbstractConfigContainer getByClass(Class<?> clazz) {
        return BY_CLASS.get(clazz);
    }
}
