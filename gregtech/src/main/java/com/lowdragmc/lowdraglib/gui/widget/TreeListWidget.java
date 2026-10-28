package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TreeNode;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.util.Mth;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Accessors(chain = true)
public class TreeListWidget<K, T> extends Widget {
    protected int scrollOffset;
    protected List<TreeNode<K, T>> list;
    protected TreeNode<K, T> selected;
    @Setter @Nullable
    protected IGuiTexture nodeTexture;
    @Setter @Nullable
    protected IGuiTexture leafTexture;
    @Setter @Nullable
    protected Consumer<TreeNode<K, T>> onSelected;
    @Setter @Nullable
    protected Function<K, IGuiTexture> keyIconSupplier;
    @Setter @Nullable
    protected Function<K, String> keyNameSupplier;
    @Setter @Nullable
    protected Function<T, IGuiTexture> contentIconSupplier;
    @Setter @Nullable
    protected Function<T, String> contentNameSupplier;
    @Setter
    protected int lineHeight = 11;
    protected boolean canSelectNode;

    public TreeListWidget(int xPosition, int yPosition, int width, int height, TreeNode<K, T> root, Consumer<TreeNode<K, T>> onSelected) {
        super(xPosition, yPosition, width, height);
        list = new ArrayList<>();
        if (root.getChildren() != null) {
            list.addAll(root.getChildren());
        }
        this.onSelected = onSelected;
    }

    public TreeListWidget<K, T> canSelectNode(boolean canSelectNode) {
        this.canSelectNode = canSelectNode;
        return this;
    }

    public TreeListWidget<K, T> setBackground(IGuiTexture background) {
        super.setBackground(background);
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (nodeTexture != null) nodeTexture.updateTick();
        if (leafTexture != null) leafTexture.updateTick();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    public TreeNode<K, T> jumpTo(List<K> path) { return null; /* ported stub */ }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; /* ported stub */ }

    private void removeNode(TreeNode<?, T> node) {
        if(node.isLeaf()) return;
        for (TreeNode<?, T> child : node.getChildren()) {
            list.remove(child);
            removeNode(child);
        }
    }
}
