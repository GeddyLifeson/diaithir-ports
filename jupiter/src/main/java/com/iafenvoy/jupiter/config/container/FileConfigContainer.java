package com.iafenvoy.jupiter.config.container;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.ConfigSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public abstract class FileConfigContainer extends AbstractConfigContainer {
    protected final String path;

    public FileConfigContainer(Identifier id, String titleKey, String path) {
        this(id, Component.translatable(titleKey, new Object[]{}), path);
    }

    public FileConfigContainer(Identifier id, Component title, String path) {
        super(id, title);
        this.path = path;
    }

    @Override
    public String getPath() {
        return Path.of(this.path).getFileName().toString();
    }

    @Override
    public void load() {
        try {
            this.deserialize(FileUtils.readFileToString(new File(this.path), StandardCharsets.UTF_8));
        } catch (Exception e) {
            Jupiter.LOGGER.error("Failed to load config: {}", this.path, e);
            this.save();
        }
    }

    @Override
    public void save() {
        try {
            FileUtils.write(new File(this.path), this.serialize(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Jupiter.LOGGER.error("Failed to save config: {}", this.path, e);
        }
    }

    @Override
    public ConfigSource getSource() {
        return ConfigSource.JUPITER;
    }
}
