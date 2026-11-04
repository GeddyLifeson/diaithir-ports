package com.iafenvoy.jupiter.config.container.wrapper;

import com.iafenvoy.jupiter.compat.ExtraConfigHolder;
import com.iafenvoy.jupiter.config.ConfigSide;
import com.iafenvoy.jupiter.config.ConfigSource;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class ExtraConfigWrapper extends AbstractConfigContainer {
    private final ConfigSide side;
    private final ConfigSource source;
    private final String filePath;
    private final Runnable saveCaller;
    private final Boolean2ObjectFunction<Identifier> backgroundTextureProvider;

    public ExtraConfigWrapper(ExtraConfigHolder holder) {
        super(holder.getConfigId(), holder.getTitle());
        this.side = holder.getSide();
        this.source = holder.getSource();
        this.filePath = holder.getPath();
        this.saveCaller = holder::save;
        this.backgroundTextureProvider = holder::getBackgroundTexture;
        this.configTabs.addAll(holder.buildGroups());
    }

    @Override
    public String getPath() {
        return this.filePath;
    }

    @Override
    public ConfigSide getSide() {
        return this.side;
    }

    @Override
    public void init() {
    }

    @Override
    public void load() {
        //(Neo)Forge will manage this
    }

    @Override
    public void save() {
        this.saveCaller.run();
    }

    @Override
    public @Nullable Identifier getBackgroundTexture(boolean ingame) {
        return this.backgroundTextureProvider.get(ingame);
    }

    @Override
    public ConfigSource getSource() {
        return this.source;
    }
}
