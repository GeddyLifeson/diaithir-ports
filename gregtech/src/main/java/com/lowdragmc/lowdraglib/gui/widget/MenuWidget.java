package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TreeNode;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Accessors(chain = true)
public class MenuWidget<K, T> extends WidgetGroup {

    public static IGuiTexture NODE_TEXTURE = new IGuiTexture() {
        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            ColorPattern.BLACK.rectTexture().draw(graphics, mouseX, mouseY, x, y, width, height);
            Icons.RIGHT.draw(graphics, mouseX, mouseY, x + width - height + 3, y + 3, height - 6, height - 6);
        }
    };
    public static IGuiTexture LEAF_TEXTURE = ColorPattern.BLACK.rectTexture();
    public static IGuiTexture NODE_HOVER_TEXTURE = ColorPattern.T_GRAY.rectTexture();
    public static IGuiTexture BACKGROUND = new GuiTextureGroup(new ColorRectTexture(0xff3C4146), ColorPattern.GRAY.borderTexture(1));

    protected final TreeNode<K, T> root;
    protected final int nodeHeight;

    @Setter @Nullable
    protected IGuiTexture nodeTexture;
    @Setter @Nullable
    protected IGuiTexture leafTexture;
    @Setter @Nullable
    protected IGuiTexture nodeHoverTexture;
    @Setter @Nullable
    protected Consumer<TreeNode<K, T>> onNodeClicked;
    @Setter @Nullable
    protected Function<K, IGuiTexture> keyIconSupplier;
    @Setter @Nullable
    protected Function<K, String> keyNameSupplier;
    @Setter @Nullable
    protected Predicate<K> crossLinePredicate;
    @Setter
    protected boolean autoClose;

    protected Map<TreeNode<K, T>, WidgetGroup> children;
    protected MenuWidget<K, T> opened;

    public MenuWidget(int xPosition, int yPosition, int nodeHeight, TreeNode<K, T> root) {
        super(xPosition, yPosition, 100, nodeHeight);
        this.root = root;
        this.autoClose = true;
        this.nodeHeight = nodeHeight;
        this.children = new LinkedHashMap<>();
    }

    public void close(){
        if (this.parent != null) {
            this.parent.waitToRemoved(this);
        }
    }

    @Override
    public void initWidget() {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            if (autoClose && !(parent instanceof MenuWidget)) {
                close();
            }
            return false;
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseMoved(double mouseX, double mouseY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) { return false; /* ported stub */ }
}
