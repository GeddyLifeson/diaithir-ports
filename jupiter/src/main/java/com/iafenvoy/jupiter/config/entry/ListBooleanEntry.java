package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListBooleanEntry extends ListBaseEntry<Boolean> {
    protected ListBooleanEntry(Builder builder) {
        super(builder);
    }

    @Override
    public Codec<Boolean> getValueCodec() {
        return Codec.BOOL;
    }

    @Override
    public ConfigEntry<Boolean> newSingleInstance(Boolean value, int index, Runnable reload) {
        return BooleanEntry.builder(this.name, value).callback((v, r, d) -> {
            if (r) {
                this.getValue().remove(index);
                reload.run();
            } else this.getValue().set(index, v);
            this.setValue(this.getValue());
        }).build();
    }

    @Override
    public Boolean newValue() {
        return false;
    }

    @Override
    public ConfigType<List<Boolean>> getType() {
        return ConfigTypes.LIST_BOOLEAN;
    }

    @Override
    public ConfigEntry<List<Boolean>> newInstance() {
        return new Builder(this).build();
    }

    public static Builder builder(Component name, List<Boolean> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, List<Boolean> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<List<Boolean>, ListBooleanEntry, Builder> {
        public Builder(Component name, List<Boolean> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, List<Boolean> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(ListBooleanEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected ListBooleanEntry buildInternal() {
            return new ListBooleanEntry(this);
        }
    }
}
