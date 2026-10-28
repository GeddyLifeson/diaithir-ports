package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.utils.GradientColor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/5/30
 * @implNote GradientColorWidget
 */
public class GradientColorWidget extends WidgetGroup {
    protected final GradientColor gradientColor;
    protected final HsbColorWidget hsbColorWidget;
    protected int selectedAlphaPoint = -1, selectedRGBPoint = 0;
    @Setter
    protected Consumer<GradientColor> onUpdate;
    // runtime
    private boolean isDraggingAlphaPoint, isDraggingRGBPoint;
    private long lastClickTick;

    public GradientColorWidget(int x, int y, int width, GradientColor gradientColor) {
        super(x, y, width, width + 15 + 20 + 3);
        this.gradientColor = gradientColor;
        addWidget(new ImageWidget(3, 3 + 10, width - 6, 15, ColorPattern.WHITE.borderTexture(1)));

        addWidget(hsbColorWidget = new HsbColorWidget(3, 15 + 20 + 3, width - 6, width - 6)
                .setColorSupplier(() -> {
                    var t = selectedAlphaPoint >= 0 ? gradientColor.getAP().get(selectedAlphaPoint).x :
                            selectedRGBPoint >= 0 ? gradientColor.getRP().get(selectedRGBPoint).x :
                            0;
                    return gradientColor.getColor(t);
                })
                .setOnChanged(color -> {
                    if (selectedAlphaPoint >= 0) {
                        gradientColor.getAP().set(selectedAlphaPoint, new Vec2( gradientColor.getAP().get(selectedAlphaPoint).x, ColorUtils.alpha(color)));
                        notifyChanged();
                    }
                    if (selectedRGBPoint >= 0) {
                        gradientColor.getRP().set(selectedRGBPoint, new Vec2( gradientColor.getRP().get(selectedRGBPoint).x, ColorUtils.red(color)));
                        gradientColor.getGP().set(selectedRGBPoint, new Vec2( gradientColor.getGP().get(selectedRGBPoint).x, ColorUtils.green(color)));
                        gradientColor.getBP().set(selectedRGBPoint, new Vec2( gradientColor.getBP().get(selectedRGBPoint).x, ColorUtils.blue(color)));
                        notifyChanged();
                    }
                }));
        hsbColorWidget.setShowAlpha(false);
        hsbColorWidget.setShowRGB(true);
    }

    protected void notifyChanged() {
        if (onUpdate != null) onUpdate.accept(gradientColor);
    }

    private float getXPosition(float t) { return (float)0; /* ported stub */ }

    private float getXCoord(float x) { return (float)0; /* ported stub */ }

    protected void openMenu(int mouseX, int mouseY) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDraggingRGBPoint = false;
        this.isDraggingAlphaPoint = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull @Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }
}
