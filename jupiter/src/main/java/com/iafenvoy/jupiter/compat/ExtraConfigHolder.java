package com.iafenvoy.jupiter.compat;

import com.iafenvoy.jupiter.config.ConfigGroup;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ExtraConfigHolder extends ConfigMetaProvider {
    Component getTitle();

    void save();

    @Nullable
    Identifier getBackgroundTexture(boolean ingame);

    Collection<? extends ConfigGroup> buildGroups();
}
