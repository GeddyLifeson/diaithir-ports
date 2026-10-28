package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.FileNode;
import com.lowdragmc.lowdraglib.gui.util.TreeNode;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lombok.Setter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.Util;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DialogWidget extends WidgetGroup {
    protected boolean isParentInVisible;
    protected Runnable onClosed;
    @Setter
    protected boolean clickClose;

    public DialogWidget(WidgetGroup parent, boolean isClient) {
        this(0, 0, parent.getSize().width, parent.getSize().height);
        if (isClient) setClientSideWidget();
        if (autoAdd()) {
            parent.addWidget(this);
        }
    }

    public DialogWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    protected boolean autoAdd() {
        return true;
    }

    public DialogWidget setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
        return this;
    }

    public DialogWidget setParentInVisible() { return null; /* ported stub */ }

    public void close() {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            if (clickClose) {
                close();
                return true;
            }
            return false;
        }
        super.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        super.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        super.mouseWheelMove(mouseX, mouseY, scrollX, scrollY);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseMoved(double mouseX, double mouseY) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        super.mouseMoved(mouseX, mouseY);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!super.keyPressed(keyCode, scanCode, modifiers) && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            writeClientAction(-1, x->{});
            close();
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        return true;
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == -1) {
            close();
        }
    }

    public static Predicate<TreeNode<File, File>> suffixFilter(String... suffixes) {
        return node -> {
            for (String suffix : suffixes) {
                if (!(node.isLeaf() && node.getContent().isFile() && !node.getContent().getName().toLowerCase().endsWith(suffix.toLowerCase()))) {
                    return true;
                }
            }
            return false;
        };
    }

    public static WidgetGroup createContainer(DialogWidget dialog, int x, int y, int width, int height, String titleText) {
        WidgetGroup title, content;
        dialog.addWidget(title = new WidgetGroup(x, y, width, 15));
        title.setBackground(new GuiTextureGroup(ColorPattern.RED.rectTexture().setTopRadius(5f), ColorPattern.GRAY.borderTexture(-1).setTopRadius(5f), new TextTexture(titleText).setWidth(width).setDropShadow(false).setType(TextTexture.TextType.ROLL)));
        dialog.addWidget(content = new WidgetGroup(x, y + 15, width, height - 15));
        content.setBackground(new GuiTextureGroup(ColorPattern.BLACK.rectTexture().setBottomRadius(5f), ColorPattern.GRAY.borderTexture(-1).setBottomRadius(5f)));
        return content;
    }

    public static WidgetGroup createContainer(DialogWidget dialog, int width, int height, String titleText) { return null; /* ported stub */ }

    public static TextFieldWidget createTextField(WidgetGroup parent, int x, int y, int width, int height) {
        TextFieldWidget textFieldWidget;
        parent.addWidget(new ImageWidget(x, y, width, height, ColorPattern.T_GRAY.rectTexture().setRadius(height / 2f)));
        parent.addWidget(textFieldWidget = new TextFieldWidget(x + 3, y, width - 6, height, null, null));
        textFieldWidget.setBordered(false);
        return textFieldWidget;
    }

    public static TextTexture createText(WidgetGroup parent, int x, int y, int width, int height) {
        TextTexture textTexture;
        parent.addWidget(new ImageWidget(x, y, width, height, textTexture = new TextTexture().setWidth(width)));
        return textTexture;
    }

    public static ButtonWidget createButton(WidgetGroup parent, int x, int y, int width, int height, String text, Runnable onClick) {
        ButtonWidget buttonWidget;
        parent.addWidget(buttonWidget = new ButtonWidget(x, y, width, height,
                new GuiTextureGroup(ColorPattern.T_GRAY.rectTexture().setRadius(height / 2f),
                        new TextTexture(text).setWidth(width).setDropShadow(false).setType(TextTexture.TextType.ROLL)),
                cd -> onClick.run())
                .setHoverTexture(new GuiTextureGroup(ColorPattern.GRAY.rectTexture().setRadius(height / 2f),
                        new TextTexture(text).setWidth(width).setDropShadow(false).setType(TextTexture.TextType.ROLL))));
        return buttonWidget;
    }

    public static DialogWidget showStringEditorDialog(WidgetGroup parent, String title, String initial, @Nullable Predicate<String> predicate, Consumer<String> result) { return null; /* ported stub */ }
    public static DialogWidget showNotification(WidgetGroup parent, String title, String info) {
        return showNotification(parent, title, info, 200, 100, null);
    }

    public static DialogWidget showNotification(WidgetGroup parent, String title, String info, int width, int height, Runnable onClosed) { return null; /* ported stub */ }

    public static DialogWidget showCheckBox(WidgetGroup parent, String title, String info, BooleanConsumer onClosed) {
        return showCheckBox(parent, title, info, 200, 100, onClosed);
    }

    public static DialogWidget showCheckBox(WidgetGroup parent, String title, String info, int width, int height, BooleanConsumer onClosed) { return null; /* ported stub */ }

    public static DialogWidget showItemSelector(WidgetGroup parent, String title, ItemStack init, Consumer<Item> itemConsumer) { return null; /* ported stub */ }

    public static DialogWidget showFileDialog(WidgetGroup parent, String title, File dir, boolean isSelector, Predicate<TreeNode<File, File>> valid, Consumer<File> result) { return null; /* ported stub */ }
}
