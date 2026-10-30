package com.iafenvoy.jupiter.config;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.mojang.serialization.*;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ConfigGroup {
    public static final ConfigGroup EMPTY = new ConfigGroup("", Component.empty());
    private final String id;
    private final Component name;
    private final List<ConfigEntry<?>> configs;
    private Codec<ConfigGroup> cache;

    public ConfigGroup(String id, Component name) {
        this(id, name, new LinkedList<>());
    }

    public ConfigGroup(String id, Component name, List<ConfigEntry<?>> configs) {
        this.id = id;
        this.name = name;
        this.configs = configs;
    }

    public ConfigGroup addEntry(ConfigEntry<?> config) {
        this.configs.add(config);
        this.cache = null;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public List<ConfigEntry<?>> getConfigs() {
        return this.configs;
    }

    @SuppressWarnings("unchecked")
    public ConfigGroup copy() {
        return new ConfigGroup(this.id, this.name, (List<ConfigEntry<?>>) (Object) this.configs.stream().map(ConfigEntry::newInstance).toList());
    }

    public Codec<ConfigGroup> getCodec(ConfigDataFixer dataFixer) {
        return new EntriesCodec(this, dataFixer).codec();
    }

    public <R> DataResult<R> encode(ConfigDataFixer dataFixer, DynamicOps<R> ops) {
        if (this.cache == null) this.cache = this.getCodec(dataFixer);
        return this.cache.encodeStart(ops, this);
    }

    public <R> void decode(ConfigDataFixer dataFixer, DynamicOps<R> ops, R input) {
        if (this.cache == null) this.cache = this.getCodec(dataFixer);
        this.cache.parse(ops, input).resultOrPartial(Jupiter.LOGGER::error).orElseThrow();
    }

    private static class EntriesCodec extends MapCodec<ConfigGroup> {
        private final ConfigGroup parent;
        private final ConfigDataFixer dataFixer;

        private EntriesCodec(ConfigGroup parent, ConfigDataFixer dataFixer) {
            this.parent = parent;
            this.dataFixer = dataFixer;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return this.parent.configs.stream().map(ConfigEntry::getKey).filter(Objects::nonNull).map(ops::createString);
        }

        @Override
        public <T> RecordBuilder<T> encode(ConfigGroup input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.configs.stream().reduce(prefix, (p, c) -> c.getKey() == null ? p : p.add(c.getKey(), c.encode(this.dataFixer, ops)), (a, b) -> null);
        }

        @Override
        public <T> DataResult<ConfigGroup> decode(DynamicOps<T> ops, MapLike<T> input) {
            input.entries().forEach(x -> ops.getStringValue(x.getFirst()).resultOrPartial(Jupiter.LOGGER::error).map(this.dataFixer::fixKey).ifPresent(s -> this.parent.configs.stream().filter(y -> Objects.equals(y.getKey(), s)).forEach(y -> y.decode(this.dataFixer, ops, x.getSecond()))));
            return DataResult.success(this.parent);
        }
    }
}
