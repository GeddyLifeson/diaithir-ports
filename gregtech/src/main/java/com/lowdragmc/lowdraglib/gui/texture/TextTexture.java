package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.annotation.*;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@LDLRegister(name = "text_texture", group = "texture")
public class TextTexture extends TransformTexture {

    @Configurable
    public String text;

    @Configurable
    @NumberColor
    public int color;

    @Configurable
    @NumberColor
    public int backgroundColor;

    @Configurable(tips = "ldlib.gui.editor.tips.image_text_width")
    @NumberRange(range = {1, Integer.MAX_VALUE})
    public int width;
    @Configurable
    @NumberRange(range = {0, Integer.MAX_VALUE})
    @Setter
    public float rollSpeed = 1;
    @Configurable
    public boolean dropShadow;

    @Configurable(tips = "ldlib.gui.editor.tips.image_text_type")
    public TextType type;

    public Supplier<String> supplier;
    @OnlyIn(Dist.CLIENT)
    private List<String> texts;

    private long lastTick;

    public TextTexture() {
        this("A", -1);
        setWidth(50);
    }

    public TextTexture(String text, int color) {
        this.color = color;
        this.type = TextType.NORMAL;
        if (LDLib.isClient()) {
            this.text = LocalizationUtils.format(text);
            texts = Collections.singletonList(this.text);
        }
    }

    public TextTexture(String text) {
        this(text, -1);
        setDropShadow(true);
    }

    public TextTexture(Supplier<String> text) {
        this("", -1);
        setSupplier(text);
        setDropShadow(true);
    }

    public TextTexture setSupplier(Supplier<String> supplier) {
        this.supplier = supplier;
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
        if (supplier != null) {
            updateText(supplier.get());
        }
    }

    @ConfigSetter(field = "text")
    public void updateText(String text) {
        if (LDLib.isClient()) {
            this.text = LocalizationUtils.format(text);
            texts = Collections.singletonList(this.text);
            setWidth(this.width);
        }
    }

    public TextTexture setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public TextTexture setColor(int color) {
        this.color = color;
        return this;
    }

    public TextTexture setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public TextTexture setWidth(int width) {
        this.width = width;
        if (LDLib.isClient()) {
            if (this.width > 0) {
                texts = Minecraft.getInstance()
                        .font.getSplitter()
                        .splitLines(text, width, Style.EMPTY)
                        .stream().map(FormattedText::getString)
                        .collect(Collectors.toList());
                if (texts.isEmpty()) {
                    texts = Collections.singletonList(text);
                }
            } else {
                texts = Collections.singletonList(text);
            }
        }
        return this;
    }

    public TextTexture setType(TextType type) {
        this.type = type;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    private void drawRollTextLine(GuiGraphics graphics, float x, float y, int width, int height, Font fontRenderer, int textH, String line) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    private void drawTextLine(GuiGraphics graphics, float x, float y, int width, int height, Font fontRenderer, int textH, String line) {
        int textW = fontRenderer.width(line);
        float _x = x + (width - textW) / 2f;
        float _y = y + (height - textH) / 2f;
        graphics.drawString(fontRenderer, line, (int) _x, (int) _y, color, dropShadow);
    }

    public enum TextType{
        NORMAL,
        HIDE,
        ROLL,
        ROLL_ALWAYS,
        LEFT,
        RIGHT,
        LEFT_HIDE,
        LEFT_ROLL,
        LEFT_ROLL_ALWAYS
    }
}
