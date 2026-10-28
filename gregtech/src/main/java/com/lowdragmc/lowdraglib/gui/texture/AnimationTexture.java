package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberColor;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.File;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

/**
 * @author KilaBash
 * @date 2022/9/14
 * @implNote AnimationTexture
 */
@LDLRegister(name = "animation_texture", group = "texture")
public class AnimationTexture extends TransformTexture {

    @Configurable(name = "ldlib.gui.editor.name.resource")
    public Identifier imageLocation;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_size")
    @NumberRange(range = {1, Integer.MAX_VALUE})
    @Getter
    protected int cellSize;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_from")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    @Getter
    protected int from;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_to")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    @Getter
    protected int to;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_animation")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    @Getter
    protected int animation;

    @Configurable
    @NumberColor
    @Getter
    protected int color = -1;

    protected int currentFrame;

    protected int currentTime;
    private long lastTick;

    public AnimationTexture() {
        this("ldlib:textures/gui/particles.png");
        setCellSize(8).setAnimation(32,  44).setAnimation(1);
    }

    public AnimationTexture(String imageLocation) {
        this.imageLocation = Identifier.parse(imageLocation);
    }

    public AnimationTexture(Identifier imageLocation) {
        this.imageLocation = imageLocation;
    }

    public AnimationTexture copy() {
        return new AnimationTexture(imageLocation).setCellSize(cellSize).setAnimation(from, to).setAnimation(animation).setColor(color);
    }

    public AnimationTexture setTexture(String imageLocation) {
        this.imageLocation = Identifier.parse(imageLocation);
        return this;
    }

    public AnimationTexture setCellSize(int cellSize) {
        this.cellSize = cellSize;
        return this;
    }

    public AnimationTexture setAnimation(int from, int to) {
        this.currentFrame = from;
        this.from = from;
        this.to = to;
        return this;
    }

    public AnimationTexture setAnimation(int animation) {
        this.animation = animation;
        return this;
    }

    @Override
    public AnimationTexture setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateTick() {
        if (com.lowdragmc.lowdraglib.utils.ClientLevelAccess.level() != null) {
            long tick = com.lowdragmc.lowdraglib.utils.ClientLevelAccess.level().getGameTime();
            if (tick == lastTick) return;
            lastTick = tick;
        }
        if (currentTime >= animation) {
            currentTime = 0;
            currentFrame += 1;
        } else {
            currentTime++;
        }
        if (currentFrame > to) {
            currentFrame = from;
        } else if (currentFrame < from) {
            currentFrame = from;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {  /* ported stub */ }

    @Override
    public void createPreview(ConfiguratorGroup father) {  /* ported stub */ }

    private Identifier getTextureFromFile(File path, File r){
        var id = path.getPath().replace('\\', '/').split("assets/")[1].split("/")[0];
        return Identifier.fromNamespaceAndPath(id, r.getPath().replace(path.getPath(), "textures").replace('\\', '/'));
    }

    @OnlyIn(Dist.CLIENT)
    protected void drawGuides(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        float cell = 1f / this.cellSize;
        int X = from % cellSize;
        int Y = from / cellSize;

        float imageU = X * cell;
        float imageV = Y * cell;

        new ColorBorderTexture(-1, 0xff00ff00).draw(graphics, 0, 0,
                x + width * imageU, y + height * imageV,
                (int) (width * (cell)), (int) (height * (cell)));

        X = to % cellSize;
        Y = to / cellSize;

        imageU = X * cell;
        imageV = Y * cell;

        new ColorBorderTexture(-1, 0xffff0000).draw(graphics, 0, 0,
                x + width * imageU, y + height * imageV,
                (int) (width * (cell)), (int) (height * (cell)));
    }
}
