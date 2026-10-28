package com.lowdragmc.lowdraglib.gui.compass;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/7/28
 * @implNote ICompassUIConfig
 */
public interface ICompassUIConfig {

    static ICompassUIConfig getDefault() { return null; /* ported stub */ }

    @Nonnull
    IGuiTexture getSectionBackground();

    @Nonnull
    IGuiTexture getListViewBackground();

    @Nonnull
    IGuiTexture getListItemBackground();

    @Nonnull
    IGuiTexture getListItemSelectedBackground();

    @Nonnull
    IGuiTexture getNodeBackground();

    @Nonnull
    IGuiTexture getNodeHoverBackground();

}
