package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.IConfiguratorContainer;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.editor.ui.ConfigPanel;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote ConfiguratorGroup
 */
public class ConfiguratorGroup extends Configurator {
    @Setter
    protected boolean canCollapse = true;
    @Getter
    protected boolean isCollapse;
    @Getter
    protected List<Configurator> configurators = new ArrayList<>();

    public ConfiguratorGroup(String name) {
        this(name, true);
    }

    public ConfiguratorGroup(String name, boolean isCollapse) {
        super(name);
        this.isCollapse = isCollapse;
    }

    @Override
    public void setConfiguratorContainer(IConfiguratorContainer configuratorContainer) {
        super.setConfiguratorContainer(configuratorContainer);
        for (Configurator configurator : configurators) {
            configurator.setConfiguratorContainer(configuratorContainer);
        }
    }

    protected void clickName(ClickData clickData) {
        if (!canCollapse) return;
        setCollapse(!isCollapse);
    }

    public void setCollapse(boolean collapse) {  /* ported stub */ }

    public void addConfigurator(int index, Configurator configurator) {  /* ported stub */ }

    public void removeConfigurator(Configurator configurator) {
        if (configurators.remove(configurator)) {
            removeWidget(configurator);
        }
    }

    public void removeAllConfigurators() {
        for (Configurator configurator : configurators) {
            removeWidget(configurator);
        }
        configurators.clear();
    }

    public void addConfigurators(Configurator... configurators) {  /* ported stub */ }

    @Override
    public void computeHeight() {  /* ported stub */ }

    @Override
    public void init(int width) {
        super.init(width);
        this.addWidget(new ButtonWidget(0, 0, leftWidth + 9, 15, IGuiTexture.EMPTY, this::clickName));
        for (Configurator configurator : configurators) {
            configurator.init(Math.max(0, width - 5));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull @Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }
}
