package com.iafenvoy.jupiter.compat.clothconfig;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.compat.ExtraConfigHolder;
import com.iafenvoy.jupiter.config.ConfigGroup;
import com.iafenvoy.jupiter.config.ConfigSide;
import com.iafenvoy.jupiter.config.ConfigSource;
import com.iafenvoy.jupiter.config.entry.*;
import com.iafenvoy.jupiter.config.interfaces.ConfigBuilder;
import com.iafenvoy.jupiter.util.JupiterUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigManager;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

@SuppressWarnings("UnstableApiUsage")
public final class ClothConfigHolder<D extends ConfigData> implements ExtraConfigHolder {
    private final ConfigManager<D> manager;
    private final String modId;
    private final D values, defaults;

    public ClothConfigHolder(ConfigManager<D> manager) {
        this.manager = manager;
        this.modId = this.manager.getDefinition().name().toLowerCase(Locale.ROOT);
        this.values = manager.getConfig();
        this.defaults = manager.getSerializer().createDefault();
    }

    public String getModId() {
        return this.modId;
    }

    @Override
    public Identifier getConfigId() {
        return Identifier.fromNamespaceAndPath(this.modId, "config");
    }

    @Override
    public String getPath() {
        return String.format(Locale.ROOT, "%s.json", this.modId);
    }

    public String baseTranslateKey() {
        return String.format(Locale.ROOT, "text.autoconfig.%s", this.modId);
    }

    @Override
    public Component getTitle() {
        return Component.translatable(String.format(Locale.ROOT, "%s.title", this.baseTranslateKey()), new Object[]{});
    }

    @Override
    public ConfigSide getSide() {
        return ConfigSide.UNKNOWN;
    }

    @Override
    public ConfigSource getSource() {
        return ConfigSource.CLOTH_CONFIG;
    }

    @Override
    public void save() {
        this.manager.save();
    }

    @Override
    public @Nullable Identifier getBackgroundTexture(boolean ingame) {
        Config.Gui.Background background = this.values.getClass().getAnnotation(Config.Gui.Background.class);
        if (background == null) return null;
        String id = background.value();
        if (Config.Gui.Background.TRANSPARENT.equals(id)) return null;
        return Identifier.tryParse(id);
    }

    @Override
    public Collection<? extends ConfigGroup> buildGroups() {
        return List.of(this.buildGroup(this.getConfigId().toString(), "", this.defaults, this.values));
    }

    public <T> ConfigGroup buildGroup(String id, String parentKey, T defaults, T values) {
        ConfigGroup group = new ConfigGroup(id, Component.translatable(String.format(Locale.ROOT, "%s.category%s", this.baseTranslateKey(), parentKey), new Object[]{}));
        for (Field field : defaults.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || !field.canAccess(defaults) || field.getAnnotation(me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded.class) != null)
                continue;
            try {
                String nameKey = String.format(Locale.ROOT, "%s.option%s.%s", this.baseTranslateKey(), parentKey, field.getName());
                ConfigBuilder<?, ?, ?> builder = this.process(nameKey, defaults, values, field);
                if (builder == null)
                    builder = ConfigGroupEntry.builder(String.format(Locale.ROOT, "%s.category%s.%s", this.baseTranslateKey(), parentKey, field.getName()), this.buildGroup(field.getName(), String.format(Locale.ROOT, "%s.%s", parentKey, field.getName()), field.get(defaults), field.get(values)));
                if (field.getAnnotation(me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip.class) != null)
                    builder.tooltip(String.format(Locale.ROOT, "%s.@Tooltip", nameKey));
                group.addEntry(builder.build());
            } catch (Exception e) {
                Jupiter.LOGGER.error("Failed to load field {} class {} from class {}", field.getName(), field.getType(), defaults.getClass().getName(), e);
            }
        }
        return group;
    }

    @SuppressWarnings("unchecked")
    private <T> ConfigBuilder<?, ?, ?> process(String nameKey, T defaults, T values, Field field) {
        AtomicReference<ConfigBuilder<?, ?, ?>> holder = new AtomicReference<>(null);
        Component name = Component.translatable(nameKey, new Object[]{});
        //Simple
        this.processEntry(holder, name, field, defaults, values, Boolean.class, BooleanEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Integer.class, IntegerEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Long.class, LongEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Double.class, DoubleEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Float.class, FloatEntry::builder);
        this.processEntry(holder, name, field, defaults, values, String.class, StringEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Enum.class, EnumEntry::builder);
        //Primitive
        this.processEntry(holder, name, field, defaults, values, Boolean.TYPE, BooleanEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Integer.TYPE, IntegerEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Long.TYPE, LongEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Double.TYPE, DoubleEntry::builder);
        this.processEntry(holder, name, field, defaults, values, Float.TYPE, FloatEntry::builder);
        //Array
        if (field.getType().isArray()) {
            this.processArrayEntry(holder, name, field, defaults, values, Boolean.class, ListBooleanEntry::builder);
            this.processArrayEntry(holder, name, field, defaults, values, Integer.class, ListIntegerEntry::builder);
            this.processArrayEntry(holder, name, field, defaults, values, Long.class, ListLongEntry::builder);
            this.processArrayEntry(holder, name, field, defaults, values, Double.class, ListDoubleEntry::builder);
            this.processArrayEntry(holder, name, field, defaults, values, String.class, ListStringEntry::builder);
        }
        if (List.class.isAssignableFrom(field.getType())) {
            this.processCollectionEntry(holder, name, field, defaults, values, Boolean.class, ListBooleanEntry::builder);
            this.processCollectionEntry(holder, name, field, defaults, values, Integer.class, ListIntegerEntry::builder);
            this.processCollectionEntry(holder, name, field, defaults, values, Long.class, ListLongEntry::builder);
            this.processCollectionEntry(holder, name, field, defaults, values, Double.class, ListDoubleEntry::builder);
            this.processCollectionEntry(holder, name, field, defaults, values, String.class, ListStringEntry::builder);
        }
        return holder.get();
    }

    private <V, T, B extends ConfigBuilder<T, ?, B>> void processEntry(AtomicReference<ConfigBuilder<?, ?, ?>> reference, Component name, Field field, V defaults, V values, Class<T> clazz, BiFunction<Component, T, B> entryProvider) {
        if (clazz.isAssignableFrom(field.getType())) {
            B builder = entryProvider.apply(name, Utils.getUnsafely(field, defaults));
            builder.callback((v, r, d) -> Utils.setUnsafely(field, values, v)).value(Utils.getUnsafely(field, values));
            reference.set(builder);
        }
    }

    @SuppressWarnings("unchecked")
    private <V, T, B extends ConfigBuilder<List<T>, ?, B>> void processArrayEntry(AtomicReference<ConfigBuilder<?, ?, ?>> reference, Component name, Field field, V defaults, V values, Class<T> clazz, BiFunction<Component, List<T>, B> entryProvider) {
        if (clazz.isAssignableFrom(field.getType().componentType())) {
            B builder = entryProvider.apply(name, List.of(Objects.requireNonNullElseGet(Utils.getUnsafely(field, defaults), () -> (T[]) Array.newInstance(clazz, 0))));
            builder.callback((v, r, d) -> Utils.setUnsafely(field, values, v.toArray((T[]) Array.newInstance(clazz, 0)))).value(List.of(Objects.requireNonNullElseGet(Utils.getUnsafely(field, values), () -> (T[]) Array.newInstance(clazz, 0))));
            reference.set(builder);
        }
    }

    private <V, T, B extends ConfigBuilder<List<T>, ?, B>> void processCollectionEntry(AtomicReference<ConfigBuilder<?, ?, ?>> reference, Component name, Field field, V defaults, V values, Class<T> clazz, BiFunction<Component, List<T>, B> entryProvider) {
        Class<?> actual = JupiterUtils.getGenericActualClass(field);
        if (actual != null && clazz.isAssignableFrom(actual)) {
            B builder = entryProvider.apply(name, Objects.requireNonNullElseGet(Utils.getUnsafely(field, defaults), List::of));
            builder.callback((v, r, d) -> Utils.setUnsafely(field, values, v)).value(Objects.requireNonNullElseGet(Utils.getUnsafely(field, values), List::of));
            reference.set(builder);
        }
    }
}
