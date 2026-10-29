package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;

/**
 * @author KilaBash
 * @date 2023/5/30
 * @implNote IToggleConfigurable
 */
public interface IToggleConfigurable extends IConfigurable {

    boolean isEnable();

    void setEnable(boolean enable);

    @Override
    default void buildConfigurator(ConfiguratorGroup father) {  /* ported stub */ }

}
