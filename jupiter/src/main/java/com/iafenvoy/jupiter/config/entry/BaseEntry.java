package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigBuilder;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ValueChangeCallback;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BaseEntry<T> implements ConfigEntry<T> {
    protected final Component name;
    protected final T defaultValue;
    @Nullable
    protected final String key;
    @Nullable
    protected Component tooltip;
    protected T value;
    protected final boolean visible, restartRequired;
    protected final List<ValueChangeCallback<T>> callbacks = new ArrayList<>();

    protected BaseEntry(Builder<T, ?, ?> builder) {
        this.name = builder.name;
        this.defaultValue = builder.defaultValue;
        this.key = builder.key;
        this.tooltip = builder.tooltip;
        this.visible = builder.visible;
        this.restartRequired = builder.restartRequired;
        this.callbacks.addAll(builder.callbacks);
        this.value = this.newDefaultValue();
    }

    @Override
    public void registerCallback(ValueChangeCallback<T> callback) {
        this.callbacks.add(callback);
    }

    @Override
    public void setValue(T value) {
        this.value = value;
        this.callbacks.forEach(x -> x.onValueChange(this.value, false, Objects.equals(this.value, this.defaultValue)));
    }

    @Override
    public @Nullable String getKey() {
        return this.key;
    }

    @Override
    public Component getName() {
        return this.restartRequired ? this.name.copy().append(" ").append(Component.translatable("jupiter.screen.restart_required", new Object[]{})) : this.name;
    }

    @Override
    public @Nullable Component getTooltip() {
        return this.tooltip;
    }

    @Override
    public T getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public void reset() {
        this.value = this.newDefaultValue();
        this.callbacks.forEach(x -> x.onValueChange(this.value, true, true));
    }

    protected T newDefaultValue() {
        return this.defaultValue;
    }

    public static abstract class Builder<T, E extends BaseEntry<T>, B extends Builder<T, E, B>> implements ConfigBuilder<T, E, B> {
        protected final Component name;
        protected final T defaultValue;
        protected T value;
        @Nullable
        protected String key;
        @Nullable
        protected Component tooltip;
        protected boolean visible = true;
        protected boolean restartRequired;
        protected final List<ValueChangeCallback<T>> callbacks = new ArrayList<>();

        public Builder(String nameKey, T defaultValue) {
            this(Component.translatable(nameKey, new Object[]{}), defaultValue);
            this.key(nameKey);
        }

        public Builder(Component name, T defaultValue) {
            this.name = name;
            this.defaultValue = this.value = defaultValue;
        }

        public Builder(E parent) {
            this.name = parent.name;
            this.defaultValue = this.value = parent.defaultValue;
            this.key = parent.key;
            this.tooltip = parent.tooltip;
            this.visible = parent.visible;
            this.restartRequired = parent.restartRequired;
            this.callbacks.addAll(parent.callbacks);
        }

        public B visible(boolean visible) {
            this.visible = visible;
            return this.self();
        }

        public B key(String key) {
            this.key = key;
            return this.self();
        }


        public B restartRequired() {
            this.restartRequired = true;
            return this.self();
        }

        @Override
        public B tooltip(String tooltipKey) {
            return this.tooltip(Component.translatable(tooltipKey, new Object[]{}));
        }

        @Override
        public B tooltip(Component tooltipKey) {
            this.tooltip = tooltipKey;
            return this.self();
        }

        @Override
        public B callback(ValueChangeCallback<T> callback) {
            this.callbacks.add(callback);
            return this.self();
        }

        @Override
        public B value(T value) {
            this.value = value;
            return this.self();
        }

        public abstract B self();

        protected abstract E buildInternal();

        @Override
        public E build() {
            E e = this.buildInternal();
            if (this.value != null) e.value = this.value;
            return e;
        }
    }
}
