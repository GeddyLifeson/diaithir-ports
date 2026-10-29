package com.lowdragmc.lowdraglib.gui.widget.codeeditor;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

@Getter
public class CodeEditorWidget extends WidgetGroup {
    public static final Identifier MONO_BOLD = LDLib.location("jetbrains_mono_bold");

    public final CodeEditor codeEditor = new CodeEditor();
    protected int scrollXOffset;
    protected int scrollYOffset;
    protected IGuiTexture xBarB = IGuiTexture.EMPTY;
    protected IGuiTexture xBarF = ColorPattern.T_GRAY.rectTexture().setRadius(2);
    protected IGuiTexture yBarB = IGuiTexture.EMPTY;
    protected IGuiTexture yBarF = ColorPattern.T_GRAY.rectTexture().setRadius(2);
    @Setter
    protected Consumer<List<String>> onTextChanged;

    // runtime
    private boolean isHoveringXBar;
    private boolean isHoveringYBar;
    private boolean isDraggingXBar;
    private boolean isDraggingYBar;
    private double lastDeltaX, lastDeltaY;

    public CodeEditorWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        setBackground(ColorPattern.DARK_GRAY.rectTexture());
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocus() && this.isActive();
    }

    public List<String> getLines() {
        return codeEditor.getLines();
    }

    public void setLines(List<String> lines) {
        this.codeEditor.setLines(lines);
    }

    public void notifyChanged() {
        if (onTextChanged != null) {
            onTextChanged.accept(codeEditor.getLines());
        }
    }

    @Nullable
    public Cursor getCursor(double mouseX, double mouseY) { return null; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHoveringXBar) {
            isDraggingXBar = true;
            return true;
        } else if (isHoveringYBar) {
            isDraggingYBar = true;
            return true;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            setFocus(true);
            codeEditor.clearSelection();
            codeEditor.setCursor(getCursor(mouseX, mouseY));
            codeEditor.startSelection();
            codeEditor.startSelection();
            return true;
        }
        if (isFocus()) {
            setFocus(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        codeEditor.endSelection();
        isDraggingXBar = false;
        isDraggingYBar = false;
        lastDeltaX = 0;
        lastDeltaY = 0;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public void adaptCursor() {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (canConsumeInput()) {
            if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                codeEditor.endSelection();
                return true;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean charTyped(char codePoint, int modifiers) {
        if (canConsumeInput()) {
            if (StringUtil.isAllowedChatCharacter(codePoint)) {
                codeEditor.insertText(Character.toString(codePoint));
                adaptCursor();
                notifyChanged();
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }
}
