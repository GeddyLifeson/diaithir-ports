package com.iafenvoy.jupiter.compat.forgeconfigspec;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.compat.ExtraConfigHolder;
import com.iafenvoy.jupiter.config.ConfigGroup;
import com.iafenvoy.jupiter.config.ConfigSide;
import com.iafenvoy.jupiter.config.ConfigSource;
import com.iafenvoy.jupiter.config.entry.*;
import com.iafenvoy.jupiter.config.interfaces.ConfigBuilder;
import com.iafenvoy.jupiter.util.JupiterUtils;
import com.iafenvoy.jupiter.util.TextFormatter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;

//WARNING!!! DO NOT try to understand how these code work!!!
public final class NightConfigHolder implements ExtraConfigHolder {
    private final String modId;
    private final ConfigSide side;
    private final String fileName;
    private final UnmodifiableConfig defaults;
    private final CommentedConfig values;
    private final Runnable save;

    public NightConfigHolder(String modId, ConfigSide side, String fileName, UnmodifiableConfig defaults, CommentedConfig values, Runnable save) {
        this.modId = modId;
        this.side = side;
        this.fileName = fileName;
        this.defaults = defaults;
        this.values = values;
        this.save = save;
    }

    @Override
    public Identifier getConfigId() {
        return Identifier.fromNamespaceAndPath(this.modId, this.side.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public Component getTitle() {
        return Component.literal(TextFormatter.formatToTitleCase(this.modId, true)).append(" ").append(Component.translatable(String.format(Locale.ROOT, "jupiter.screen.%s_config", this.side.name().toLowerCase(Locale.ROOT)), new Object[]{}));
    }

    @Override
    public ConfigSide getSide() {
        return this.side;
    }

    @Override
    public ConfigSource getSource() {
        return ConfigSource.NIGHT_CONFIG;
    }

    @Override
    public String getPath() {
        return this.fileName;
    }

    @Override
    public void save() {
        this.save.run();
    }

    @Override
    public @Nullable Identifier getBackgroundTexture(boolean ingame) {
        return null;
    }

    @Override
    public List<ConfigGroup> buildGroups() {
        return List.of(this.buildGroup(this.getConfigId().toString(), this.getTitle(), this.defaults, this.values));
    }

    public ConfigGroup buildGroup(String id, Component groupName, UnmodifiableConfig defaults, CommentedConfig values) {
        ConfigGroup group = new ConfigGroup(id, groupName);
        for (UnmodifiableConfig.Entry entry : defaults.entrySet()) {
            Object entryValue = entry.getValue(), value = values.get(entry.getKey());
            if (entryValue instanceof ModConfigSpec.ValueSpec spec) {
                Object defaultValue = spec.getDefault();
                try {
                    String translateKey = Objects.requireNonNullElseGet(spec.getTranslationKey(), entry::getKey);
                    String fallback = TextFormatter.formatToTitleCase(translateKey, false);
                    ConfigBuilder<?, ?, ?> builder = this.process(values, Component.translatableWithFallback(translateKey, fallback, new Object[]{}), entry, defaultValue, value, JupiterUtils.packPredicate(spec::test));
                    if (builder == null)
                        Jupiter.LOGGER.warn("Cannot find suitable entry for key={}, type={} in config={}:{}", entry.getKey(), defaultValue.getClass().getName(), this.modId, this.side);
                    else {
                        if (builder instanceof BaseEntry.Builder<?, ?, ?> baseBuilder) {
                            baseBuilder.key(entry.getKey());
                            if (spec.getComment() != null) baseBuilder.tooltip(spec.getComment());
                        }
                        group.addEntry(builder.build());
                    }
                } catch (Exception e) {
                    Jupiter.LOGGER.error("Cannot load key={}, type={} in config={}:{}", entry.getKey(), defaultValue.getClass().getName(), this.modId, this.side, e);
                }
            } else if (entryValue instanceof UnmodifiableConfig spec && value instanceof CommentedConfig config) {
                String text = entry.getKey();
                String fallback = TextFormatter.formatToTitleCase(entry.getKey(), false);
                Component name = Component.translatableWithFallback(text, fallback, new Object[]{});
                group.addEntry(ConfigGroupEntry.builder(name, this.buildGroup(entry.getKey(), name, spec, config)).key(entry.getKey()).build());
            }
        }
        return group;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ConfigBuilder<?, ?, ?> process(CommentedConfig values, Component name, UnmodifiableConfig.Entry entry, Object defaultValue, Object value, Predicate<Object> validator) {
        AtomicReference<ConfigBuilder<?, ?, ?>> holder = new AtomicReference<>(null);
        //Simple
        this.processEntry(holder, values, name, entry, defaultValue, value, Boolean.class, BooleanEntry::builder);
        this.processEntry(holder, values, name, entry, defaultValue, value, Integer.class, IntegerEntry::builder);
        this.processEntry(holder, values, name, entry, defaultValue, value, Long.class, LongEntry::builder);
        this.processEntry(holder, values, name, entry, defaultValue, value, Double.class, DoubleEntry::builder);
        this.processEntry(holder, values, name, entry, defaultValue, value, String.class, StringEntry::builder);
        //Enum
        this.processEnum(holder, values, name, entry, defaultValue, value, defaultValue.getClass());
        //List
        if (Collection.class.isAssignableFrom(defaultValue.getClass()))
            //Some magic hack
            if (validator.test(List.of("")))//Check string first to avoid some authors don't write validator
                this.<String, ListStringEntry.Builder>processCollectionEntry(holder, values, name, entry, defaultValue, value, ListStringEntry::builder);
            else if (validator.test(List.of(false)))
                this.<Boolean, ListBooleanEntry.Builder>processCollectionEntry(holder, values, name, entry, defaultValue, value, ListBooleanEntry::builder);
            else if (validator.test(List.of(0)))
                this.<Integer, ListIntegerEntry.Builder>processCollectionEntry(holder, values, name, entry, defaultValue, value, ListIntegerEntry::builder);
            else if (validator.test(List.of(0L)))
                this.<Long, ListLongEntry.Builder>processCollectionEntry(holder, values, name, entry, defaultValue, value, ListLongEntry::builder);
            else if (validator.test(List.of(0D)))
                this.<Double, ListDoubleEntry.Builder>processCollectionEntry(holder, values, name, entry, defaultValue, value, ListDoubleEntry::builder);
            else {//This method is unstable and usually failed to get
                Optional<?> any = ((List<?>) defaultValue).stream().findAny();
                if (any.isPresent() && any.get().getClass().isEnum())
                    this.processEnumCollection(holder, values, name, entry, defaultValue, value, (Enum) any.get());
                else {
                    ConfigSpecLoader.meetEmptyEnumList();
                    holder.set(SeparatorEntry.builder().text("jupiter.screen.cannot_process_list_enum").tooltip(name));
                }
            }
        return holder.get();
    }

    @SuppressWarnings("unchecked")
    private <T, B extends ConfigBuilder<T, ?, B>> void processEntry(AtomicReference<ConfigBuilder<?, ?, ?>> reference, CommentedConfig values, Component name, UnmodifiableConfig.Entry entry, Object defaultValue, Object value, Class<T> clazz, BiFunction<Component, T, B> entryProvider) {
        if (clazz.isAssignableFrom(defaultValue.getClass()) && clazz.isAssignableFrom(value.getClass())) {
            B builder = entryProvider.apply(name, (T) defaultValue);
            builder.callback((v, _, _) -> values.set(entry.getKey(), v)).value((T) value);
            reference.set(builder);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void processEnum(AtomicReference<ConfigBuilder<?, ?, ?>> reference, CommentedConfig values, Component name, UnmodifiableConfig.Entry entry, Object defaultValue, Object value, Class<?> clazz) {
        if (clazz.isEnum() && clazz.isAssignableFrom(defaultValue.getClass()) && value instanceof String valueStr) {
            Class<T> testClazz = (Class<T>) clazz;
            EnumEntry.Builder<T> builder = EnumEntry.builder(name, (T) defaultValue);
            builder.callback((v, _, _) -> values.set(entry.getKey(), v.name())).value(Enum.valueOf(testClazz, valueStr));
            reference.set(builder);
        }
    }

    @SuppressWarnings("unchecked")
    private <T, B extends ConfigBuilder<List<T>, ?, B>> void processCollectionEntry(AtomicReference<ConfigBuilder<?, ?, ?>> reference, CommentedConfig values, Component name, UnmodifiableConfig.Entry entry, Object defaultValue, Object value, BiFunction<Component, List<T>, B> entryProvider) {
        B builder = entryProvider.apply(name, (List<T>) defaultValue);
        builder.callback((v, _, _) -> values.set(entry.getKey(), v));
        if (value != null) builder.value(new LinkedList<>((List<T>) value));
        reference.set(builder);
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void processEnumCollection(AtomicReference<ConfigBuilder<?, ?, ?>> reference, CommentedConfig values, Component name, UnmodifiableConfig.Entry entry, Object defaultValue, Object value, T any) {
        Class<T> clazz = any.getDeclaringClass();
        ListEnumEntry.Builder<T> builder = ListEnumEntry.builder(name, ((List<T>) defaultValue), any);
        builder.callback((v, _, _) -> values.set(entry.getKey(), v.stream().map(Enum::name).toList())).value(new LinkedList<>(((List<String>) value).stream().map(x -> Enum.valueOf(clazz, x)).toList()));
        reference.set(builder);
    }
}
