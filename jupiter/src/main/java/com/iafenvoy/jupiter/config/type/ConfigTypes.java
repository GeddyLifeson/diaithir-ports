package com.iafenvoy.jupiter.config.type;

import com.iafenvoy.jupiter.config.ConfigGroup;
import com.mojang.datafixers.util.Unit;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public interface ConfigTypes {
    ConfigType<Unit> SEPARATOR = new SingleConfigType<>();
    ConfigType<ConfigGroup> CONFIG_GROUP = new SingleConfigType<>();
    ConfigType<Boolean> BOOLEAN = new SingleConfigType<>();
    ConfigType<Integer> INTEGER = new SingleConfigType<>();
    ConfigType<Long> LONG = new SingleConfigType<>();
    ConfigType<Double> DOUBLE = new SingleConfigType<>();
    ConfigType<Float> FLOAT = new SingleConfigType<>();
    ConfigType<String> STRING = new SingleConfigType<>();
    @ApiStatus.Internal
    ConfigType<Map.Entry<String, String>> ENTRY_STRING = new SingleConfigType<>();
    @ApiStatus.Internal
    ConfigType<Map.Entry<String, Integer>> ENTRY_INTEGER = new SingleConfigType<>();
    @ApiStatus.Internal
    ConfigType<Map.Entry<String, Double>> ENTRY_DOUBLE = new SingleConfigType<>();
    ConfigType<Enum<?>> ENUM = new SingleConfigType<>();
    ConfigType<List<Boolean>> LIST_BOOLEAN = new ListConfigType<>(BOOLEAN);
    ConfigType<List<Integer>> LIST_INTEGER = new ListConfigType<>(INTEGER);
    ConfigType<List<Long>> LIST_LONG = new ListConfigType<>(LONG);
    ConfigType<List<Double>> LIST_DOUBLE = new ListConfigType<>(DOUBLE);
    ConfigType<List<String>> LIST_STRING = new ListConfigType<>(STRING);
    ConfigType<List<Enum<?>>> LIST_ENUM = new ListConfigType<>(ENUM);
    ConfigType<Map<String, String>> MAP_STRING = new MapConfigType<>(STRING);
    ConfigType<Map<String, Integer>> MAP_INTEGER = new MapConfigType<>(INTEGER);
    ConfigType<Map<String, Double>> MAP_DOUBLE = new MapConfigType<>(DOUBLE);
    ConfigType<Identifier> RESOURCE_LOCATION = new SingleConfigType<>();
}
