package com.iafenvoy.jupiter.config.container;

import com.google.gson.*;
import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.ConfigDataFixer;
import com.iafenvoy.jupiter.config.ConfigGroup;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.util.Comment;
import com.mojang.serialization.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractConfigContainer implements ConfigMetaProvider {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final List<ConfigGroup> configTabs = new ArrayList<>();
    protected final Identifier id;
    protected final Component title;
    protected final ConfigDataFixer dataFixer = new ConfigDataFixer();
    @Nullable
    private Codec<List<ConfigGroup>> codecCache;

    public AbstractConfigContainer(Identifier id, Component title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public Identifier getConfigId() {
        return this.id;
    }

    public Component getTitle() {
        return this.title;
    }

    public ConfigGroup createTab(String id, String translateKey) {
        return this.createTab(id, Component.translatable(translateKey, new Object[]{}));
    }

    public ConfigGroup createTab(String id, Component name) {
        ConfigGroup category = new ConfigGroup(id, name, new ArrayList<>());
        this.configTabs.add(category);
        this.codecCache = null;
        return category;
    }

    public List<ConfigGroup> getConfigTabs() {
        return this.configTabs;
    }

    public final void onConfigsChanged() {
        this.save();
        this.load();
    }

    public abstract void init();

    public abstract void load();

    public abstract void save();

    public String serialize() {
        return GSON.toJson(this.getCodec().encodeStart(JsonOps.INSTANCE, this.configTabs).resultOrPartial(Jupiter.LOGGER::error).orElseThrow());
    }

    @Comment("For Network Usage Only")
    public final CompoundTag serializeNbt() {
        return (CompoundTag) this.getCodec().encodeStart(NbtOps.INSTANCE, this.configTabs).resultOrPartial(Jupiter.LOGGER::error).orElseThrow();
    }

    public void deserialize(String data) {
        JsonElement element = JsonParser.parseString(data);
        if (element instanceof JsonObject obj) this.getCodec().parse(JsonOps.INSTANCE, obj);
    }

    @Comment("For Network Usage Only")
    public final void deserializeNbt(CompoundTag element) {
        if (element == null) return;
        this.getCodec().parse(NbtOps.INSTANCE, element);
    }

    @NotNull
    public Codec<List<ConfigGroup>> getCodec() {
        if (this.codecCache == null) this.codecCache = this.buildCodec();
        return this.codecCache;
    }

    protected Codec<List<ConfigGroup>> buildCodec() {
        return new GroupsCodec(this).codec();
    }

    @Nullable
    public Identifier getBackgroundTexture(boolean ingame) {
        return null;
    }

    private static class GroupsCodec extends MapCodec<List<ConfigGroup>> {
        private final AbstractConfigContainer parent;

        private GroupsCodec(AbstractConfigContainer parent) {
            this.parent = parent;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return this.parent.configTabs.stream().map(ConfigGroup::getId).map(ops::createString);
        }

        @Override
        public <T> RecordBuilder<T> encode(List<ConfigGroup> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.stream().reduce(prefix, (p, c) -> p.add(c.getId(), c.encode(this.parent.dataFixer, ops)), (a, b) -> null);
        }

        @Override
        public <T> DataResult<List<ConfigGroup>> decode(DynamicOps<T> ops, MapLike<T> input) {
            input.entries().forEach(x -> ops.getStringValue(x.getFirst()).resultOrPartial(Jupiter.LOGGER::error).map(this.parent.dataFixer::fixKey).ifPresent(s -> this.parent.configTabs.stream().filter(y -> Objects.equals(y.getId(), s)).forEach(y -> y.decode(this.parent.dataFixer, ops, x.getSecond()))));
            return DataResult.success(this.parent.configTabs);
        }
    }
}
