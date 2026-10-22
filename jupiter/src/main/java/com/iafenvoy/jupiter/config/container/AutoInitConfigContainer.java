package com.iafenvoy.jupiter.config.container;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.ConfigGroup;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;

public class AutoInitConfigContainer extends FileConfigContainer {
    public AutoInitConfigContainer(Identifier id, String titleKey, String path) {
        super(id, titleKey, path);
    }

    public AutoInitConfigContainer(Identifier id, Component title, String path) {
        super(id, title, path);
    }

    @Override
    public void init() {
        for (Field field : this.getClass().getFields())
            if (AutoInitConfigCategoryBase.class.isAssignableFrom(field.getType()))
                try {
                    this.configTabs.add(((AutoInitConfigCategoryBase) field.get(this)).getCategory());
                } catch (Exception e) {
                    Jupiter.LOGGER.error("Failed to auto init category {}", field.getName(), e);
                }
    }

    public static class AutoInitConfigCategoryBase {
        private final ConfigGroup category;
        private boolean loaded = false;

        public AutoInitConfigCategoryBase(String id, String translateKey) {
            this(id, Component.translatable(translateKey, new Object[]{}));
        }

        public AutoInitConfigCategoryBase(String id, Component name) {
            this.category = new ConfigGroup(id, name);
        }

        @SuppressWarnings("removal")
        public ConfigGroup getCategory() {
            if (!this.loaded) {
                this.loaded = true;
                for (Field field : this.getClass().getFields())
                    if (ConfigEntry.class.isAssignableFrom(field.getType()))
                        try {
                            this.category.addEntry((ConfigEntry<?>) field.get(this));
                        } catch (Exception e) {
                            Jupiter.LOGGER.error("Failed to auto init config key {}", field.getName(), e);
                        }
            }
            return this.category;
        }
    }
}
