package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

@LDLRegister(name = "draggable_scrollable_group", group = "widget.group")
@Accessors(chain = true)
public class DraggableScrollableWidgetGroup extends WidgetGroup {
    public enum ScrollWheelDirection {
        VERTICAL,
        HORIZONTAL,
    }
    @Getter
    protected int scrollXOffset;
    @Getter
    protected int scrollYOffset;
    @Configurable(name = "ldlib.gui.editor.name.x_bar_height")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    protected int xBarHeight;
    @Configurable(name = "ldlib.gui.editor.name.y_bar_width")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    protected int yBarWidth;
    @Configurable(name = "ldlib.gui.editor.name.draggable")
    @Setter @Getter
    protected boolean draggable;
    @Configurable(name = "ldlib.gui.editor.name.scrollable")
    @Setter @Getter
    protected boolean scrollable = true;
    @Getter @Setter
    @Configurable(name = "ldlib.gui.editor.name.scroll_wheel_direction")
    protected ScrollWheelDirection scrollWheelDirection = ScrollWheelDirection.VERTICAL;
    @Getter @Setter
    @Configurable(name = "ldlib.gui.editor.name.use_scissor")
    protected boolean useScissor;
    protected int maxHeight;
    protected int maxWidth;
    @Configurable(name = "ldlib.gui.editor.name.x_bar_background")
    protected IGuiTexture xBarB;
    @Configurable(name = "ldlib.gui.editor.name.x_bar_foreground")
    protected IGuiTexture xBarF;
    @Configurable(name = "ldlib.gui.editor.name.y_bar_background")
    protected IGuiTexture yBarB;
    @Configurable(name = "ldlib.gui.editor.name.y_bar_foreground")
    protected IGuiTexture yBarF;
    protected Widget draggedWidget;
    protected Widget selectedWidget;
    protected boolean isComputingMax;
    private boolean draggedPanel;
    private boolean draggedOnXScrollBar;
    private boolean draggedOnYScrollBar;
    private double lastDeltaX, lastDeltaY;
    @Getter
    private final Set<BiConsumer<Integer, Integer>> moveCallbacks = new HashSet<>();

    public DraggableScrollableWidgetGroup() {
        this(0, 0,50, 50);
    }

    @Override
    public void initTemplate() {
        setBackground(ColorPattern.RED.rectTexture());
        setYScrollBarWidth(4).setYBarStyle(ColorPattern.RED.rectTexture(), ColorPattern.WHITE.rectTexture().setRadius(2));
    }

    public DraggableScrollableWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        maxHeight = height;
        maxWidth = width;
        useScissor = true;
    }

    @ConfigSetter(field = "xBarHeight")
    public DraggableScrollableWidgetGroup setXScrollBarHeight(int xBar) {
        this.xBarHeight = xBar;
        computeMax();
        return this;
    }

    @ConfigSetter(field = "yBarWidth")
    public DraggableScrollableWidgetGroup setYScrollBarWidth(int yBar) {
        this.yBarWidth = yBar;
        computeMax();
        return this;
    }

    public DraggableScrollableWidgetGroup setBackground(IGuiTexture background) {
        super.setBackground(background);
        return this;
    }

    public DraggableScrollableWidgetGroup setXBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.xBarB = background;
        this.xBarF = bar;
        return this;
    }

    public DraggableScrollableWidgetGroup setYBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.yBarB = background;
        this.yBarF = bar;
        return this;
    }

    @Override
    public WidgetGroup addWidget(int index, Widget widget) { return null; /* ported stub */ }

    @Override
    public void removeWidget(Widget widget) {
        super.removeWidget(widget);
        computeMax();
        if (widget == draggedWidget) draggedWidget = null;
        if (widget == selectedWidget) selectedWidget = null;
    }

    @Override
    public void clearAllWidgets() {  /* ported stub */ }

    @Override
    @ConfigSetter(field = "size")
    public void setSize(Size size) {
        super.setSize(size);
        maxHeight = Math.max(size.height - xBarHeight, maxHeight);
        maxWidth = Math.max(size.width - yBarWidth, maxWidth);
//        computeMax();
        for (Widget widget : widgets) {
            Position newPos = widget.getSelfPosition();
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
    }

    @Override
    protected void onChildSelfPositionUpdate(Widget child) {  /* ported stub */ }

    @Override
    protected void onChildSizeUpdate(Widget child) {  /* ported stub */ }

    public void computeMax() {  /* ported stub */ }

    protected int getMaxHeight() {
        return maxHeight + xBarHeight;
    }

    protected int getMaxWidth() {
        return maxWidth + yBarWidth;
    }

    public int getWidgetBottomHeight() { return (int)0; /* ported stub */ }

    public void setScrollXOffset(int scrollXOffset) {  /* ported stub */ }

    public void setScrollYOffset(int scrollYOffset) {  /* ported stub */ }

    private boolean isOnXScrollPane(double mouseX, double mouseY) { return false; /* ported stub */ }

    private boolean isOnYScrollPane(double mouseX, double mouseY) { return false; /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    protected boolean hookDrawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastDeltaX = 0;
        lastDeltaY = 0;
        if (xBarHeight > 0 && isOnXScrollPane(mouseX, mouseY)) {
            this.draggedOnXScrollBar = true;
            setFocus(true);
            return true;
        }
        else if (yBarWidth > 0 && isOnYScrollPane(mouseX, mouseY)) {
            this.draggedOnYScrollBar = true;
            setFocus(true);
            return true;
        } else if(isMouseOverElement(mouseX, mouseY)){
            if (checkClickedDragged(mouseX, mouseY, button)) {
                setFocus(true);
                return true;
            }
            setFocus(true);
            if (draggable) {
                this.draggedPanel = true;
                return true;
            }
            return false;
        }
        setFocus(false);
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    protected boolean checkClickedDragged(double mouseX, double mouseY, int button) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggedOnXScrollBar) {
            draggedOnXScrollBar = false;
        } else if (draggedOnYScrollBar) {
            draggedOnYScrollBar = false;
        } else if (draggedWidget != null) {
            ((IDraggable)draggedWidget).endDrag(mouseX, mouseY);
            draggedWidget = null;
        } else if (draggedPanel) {
            draggedPanel = false;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public List<Rect2i> getGuiExtraAreas(Rect2i guiRect, List<Rect2i> list) {
        Rect2i rect2i = toRectangleBox();
        if (rect2i.getX() < guiRect.getX()
                || rect2i.getX() + rect2i.getWidth() >  guiRect.getX() + guiRect.getWidth()
                || rect2i.getY() < guiRect.getY()
                || rect2i.getY() + rect2i.getHeight() >  guiRect.getY() + guiRect.getHeight()){
            list.add(toRectangleBox());
        }
        return list;
    }

    public void setSelected(Widget widget) {
        if (widget instanceof ISelected) {
            if (selectedWidget != null && selectedWidget != widget) {
                ((ISelected) selectedWidget).onUnSelected();
            }
            selectedWidget = widget;
            ((ISelected) selectedWidget).onSelected();
        } else if (widget == null) {
            if (selectedWidget != null) {
                ((ISelected) selectedWidget).onUnSelected();
            }
            selectedWidget = null;
        }
    }

    @Override
    public CompoundTag serializeInnerNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeInnerNBT(provider);
        tag.putInt("scrollXOffset", scrollXOffset);
        tag.putInt("scrollYOffset", scrollYOffset);
        tag.putInt("maxHeight", maxHeight);
        tag.putInt("maxWidth", maxWidth);
        return tag;
    }

    @Override
    public void deserializeInnerNBT(HolderLookup.Provider provider, CompoundTag nbt) {  /* ported stub */ }

    public interface IDraggable extends ISelected {
        default boolean allowDrag(double mouseX, double mouseY, int button) {
            return allowSelected(mouseX, mouseY, button);
        }
        default void startDrag(double mouseX, double mouseY) {}
        default boolean dragging(double mouseX, double mouseY, double deltaX, double deltaY) {return true;}
        default void endDrag(double mouseX, double mouseY) {}

        /**
         * @return if false, can not be dragged out of container size
         */
        default boolean canDragOutRange() {
            return false;
        }
    }

    public interface ISelected {
        boolean allowSelected(double mouseX, double mouseY, int button);
        default void onSelected() {}
        default void onUnSelected() {}
    }
}
