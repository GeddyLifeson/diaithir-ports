package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurable;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.editor.ui.tool.WidgetToolBox;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.WidgetTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2022/12/6
 * @implNote UIWrapper
 */
public record UIWrapper(MainPanel panel, IConfigurableWidget inner) implements IConfigurable {

    public boolean isSelected() { return false; /* ported stub */ }

    public boolean isHover() { return false; /* ported stub */ }

    public boolean checkAcceptable(UIWrapper uiWrapper) {
        return inner instanceof IConfigurableWidgetGroup group && group.canWidgetAccepted(uiWrapper.inner);
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; /* ported stub */ }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {  /* ported stub */ }

    public void remove() {  /* ported stub */ }

    public void onDragPosition(int deltaX, int deltaY) {
        inner().widget().addSelfPosition(deltaX, deltaY);
    }

    public void onDragSize(int deltaX, int deltaY) {  /* ported stub */ }

    public boolean is(IConfigurableWidget configurableWidget) {
        return inner == configurableWidget;
    }

    public IGuiTexture toDraggingTexture(int mouseX, int mouseY) {
        return new WidgetTexture(mouseX, mouseY, inner.widget());
    }
}
