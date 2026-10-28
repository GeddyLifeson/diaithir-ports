package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.lowdraglib.gui.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.*;
import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.WidgetUIAccess;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.WidgetTexture;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@LDLRegister(name = "group", group = "widget.group")
public class WidgetGroup extends Widget implements IGhostIngredientTarget, IIngredientSlot, IConfigurableWidgetGroup {

    public final List<Widget> widgets = new ArrayList<>();
    private final WidgetGroupUIAccess groupUIAccess = new WidgetGroupUIAccess();
    @Configurable(name = "ldlib.gui.editor.name.isDynamicSized", tips = "ldlib.gui.editor.tips.isDynamicSized")
    @Getter
    private boolean isDynamicSized;
    @Configurable(name = "ldlib.gui.editor.name.layout", tips = "ldlib.gui.editor.tips.layout")
    @Getter
    private Layout layout = Layout.NONE;
    @Configurable(name = "ldlib.gui.editor.name.layoutPadding", tips = "ldlib.gui.editor.tips.layoutPadding")
    @Getter
    private int layoutPadding = 0;
    @Configurable(name = "ldlib.gui.editor.name.allowXEIIngredientOverMouse", tips = "ldlib.gui.editor.tips.allowXEIIngredientOverMouse")
    @Getter
    @Setter
    private boolean allowXEIIngredientOverMouse = true;
    protected final List<Widget> waitToRemoved;
    protected final List<Widget> waitToAdded;

    public WidgetGroup() {
        this(0, 0,50, 50);
    }

    @Override
    public void initTemplate() {
        setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);
    }

    public WidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.isDynamicSized = false;
        waitToRemoved = new ArrayList<>();
        waitToAdded = new ArrayList<>();
    }

    public WidgetGroup(Position position) {
        super(position, Size.ZERO);
        this.isDynamicSized = true;
        waitToRemoved = new ArrayList<>();
        waitToAdded = new ArrayList<>();
    }

    public WidgetGroup(Position position, Size size) {
        super(position, size);
        this.isDynamicSized = false;
        waitToRemoved = new ArrayList<>();
        waitToAdded = new ArrayList<>();
    }

    @ConfigSetter(field = "layout")
    public void setLayout(Layout layout) {
        if (layout == this.layout) return;
        this.layout = layout;
        recomputeLayout();
    }

    @ConfigSetter(field = "layoutPadding")
    public void setLayoutPadding(int layoutPadding) {
        if (layoutPadding == this.layoutPadding) return;
        this.layoutPadding = layoutPadding;
        recomputeLayout();
    }

    @ConfigSetter(field = "isDynamicSized")
    public void setDynamicSized(boolean dynamicSized) {
        if (dynamicSized == this.isDynamicSized) return;
        isDynamicSized = dynamicSized;
        recomputeSize();
    }

    @Override
    public void writeInitialData(RegistryFriendlyByteBuf buffer) {  /* ported stub */ }

    @Override
    public void readInitialData(RegistryFriendlyByteBuf buffer) {  /* ported stub */ }

    @Override
    public WidgetGroup setClientSideWidget() {
        super.setClientSideWidget();
        for (Widget widget : widgets) {
            widget.setClientSideWidget();
        }
        return this;
    }

    public List<Widget> getContainedWidgets(boolean includeHidden) { return null; /* ported stub */ }

    @Override
    protected void onPositionUpdate() {  /* ported stub */ }

    @Override
    public boolean isMouseOverElement(double mouseX, double mouseY) { return false; /* ported stub */ }

    @Nullable
    @Override
    public Widget getHoverElement(double mouseX, double mouseY) { return null; /* ported stub */ }

    protected void onChildSelfPositionUpdate(Widget child) {
        recomputeLayout();
        recomputeSize();
    }

    protected void onChildSizeUpdate(Widget child) {
        recomputeLayout();
        recomputeSize();
    }

    @Nullable
        public Widget getFirstWidgetById(Pattern regex) {
        List<Widget> list = new ArrayList<>();
        getWidgetsById(list, regex);
        return list.isEmpty() ? null : list.get(0);
    }

        public List<Widget> getWidgetsById(Pattern regex) {
        List<Widget> list = new ArrayList<>();
        getWidgetsById(list, regex);
        return list;
    }

    @Nullable
    public Widget getFirstWidgetById(String regex) {
        return getFirstWidgetById(Pattern.compile(regex));
    }

    public List<Widget> getWidgetsById(String regex) {
        return getWidgetsById(Pattern.compile(regex));
    }

    private void getWidgetsById(List<Widget> list, Pattern regex) {
        for (Widget widget : widgets) {
            if (regex.matcher(widget.id).find()) {
                list.add(widget);
            }
            if (widget instanceof WidgetGroup widgetGroup) {
                widgetGroup.getWidgetsById(list, regex);
            }
        }
    }

    public <T extends Widget> List<T> getWidgetsByType(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        getWidgetsByType(list, clazz);
        return list;
    }

    private <T extends Widget> void getWidgetsByType(List<T> list, Class<T> clazz) {
        for (Widget widget : widgets) {
            if (clazz.isAssignableFrom(widget.getClass())) {
                list.add((T) widget);
            }
            if (widget instanceof WidgetGroup group) {
                group.getWidgetsByType(list, clazz);
            }
        }
    }

    protected void recomputeLayout() {
        if (layout != Layout.NONE) {
            var lastPosition = new Position(0, 0);
            switch (layout) {
                case VERTICAL_LEFT -> {
                    for (var widget : widgets) {
                        lastPosition = lastPosition.addY(layoutPadding);
                        widget.setSelfPosition(lastPosition);
                        lastPosition = lastPosition.add(0, widget.getSizeHeight());
                    }
                }
                case VERTICAL_CENTER -> {
                    for (var widget : widgets) {
                        lastPosition = lastPosition.addY(layoutPadding);
                        widget.setSelfPosition(lastPosition.add((getSizeWidth() - widget.getSizeWidth()) / 2, 0));
                        lastPosition = lastPosition.add(0, widget.getSizeHeight());
                    }
                }
                case VERTICAL_RIGHT -> {
                    for (var widget : widgets) {
                        lastPosition = lastPosition.addY(layoutPadding);
                        widget.setSelfPosition(lastPosition.add(getSizeWidth() - widget.getSizeWidth(), 0));
                        lastPosition = lastPosition.add(0, widget.getSizeHeight());
                    }
                }
                case HORIZONTAL_TOP -> {
                    for (var widget : widgets) {
                        lastPosition = lastPosition.addX(layoutPadding);
                        widget.setSelfPosition(lastPosition);
                        lastPosition = lastPosition.add(widget.getSizeWidth(), 0);
                    }
                }
                case HORIZONTAL_CENTER -> {
                    for (var widget : widgets) {
                        lastPosition = lastPosition.addX(layoutPadding);
                        widget.setSelfPosition(lastPosition.add(0, (getSizeHeight() - widget.getSizeHeight()) / 2));
                        lastPosition = lastPosition.add(widget.getSizeWidth(), 0);
                    }
                }
                case HORIZONTAL_BOTTOM -> {
                    for (var widget : widgets) {
                        lastPosition = lastPosition.addX(layoutPadding);
                        widget.setSelfPosition(lastPosition.add(0, getSizeHeight() - widget.getSizeHeight()));
                        lastPosition = lastPosition.add(widget.getSizeWidth(), 0);
                    }
                }
            }
        }
    }

    protected boolean recomputeSize() { return false; /* ported stub */ }

    protected Size computeDynamicSize() { return null; /* ported stub */ }

    public WidgetGroup setVisible(boolean visible) { return null; /* ported stub */ }

    @Override
    public void setGui(ModularUI gui) {
        super.setGui(gui);
        for (Widget widget : widgets) {
            if (widget.gui != gui) {
                widget.setGui(gui);
            }
        }
    }

    public boolean isChild(Widget widget) {
        return widget.isParent(this);
    }

    public WidgetGroup addWidget(Widget widget) {
        return addWidget(widgets.size(), widget);
    }

    public WidgetGroup addWidgets(Widget... widgets) {
        for (Widget widget : widgets) {
            addWidget(widget);
        }
        return this;
    }

    public <T extends Widget> WidgetGroup addWidget(T widget, Consumer<T> callback) {
        addWidget(widgets.size(), widget);
        callback.accept(widget);
        return this;
    }

    public WidgetGroup addWidget(int index, Widget widget) { return null; /* ported stub */ }

    public void addWidgetAnima(Widget widget, Transform animation) {
        addWidget(widget);
        widget.animation(animation.setIn());
    }

    public void removeWidgetAnima(Widget widget, Transform animation) {  /* ported stub */ }

    public void waitToRemoved(Widget widget) {
        synchronized (waitToRemoved) {
            waitToRemoved.add(widget);
        }
    }

    public void waitToAdded(Widget widget) {
        synchronized (waitToAdded) {
            waitToAdded.add(widget);
        }
    }

    public int getAllWidgetSize() {
        return widgets.size() - waitToRemoved.size() + waitToAdded.size();
    }

    public void removeWidget(Widget widget) {  /* ported stub */ }

    public void clearAllWidgets() {  /* ported stub */ }


    @Override
    public void initWidget() {
        super.initWidget();
        for (Widget widget : widgets) {
            if (widget.gui != gui) {
                widget.setGui(gui);
            }
            widget.initWidget();
        }
    }

    @Override
    public List<Target> getPhantomTargets(Object ingredient) { return null; /* ported stub */ }

    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) { return null; /* ported stub */ }

    @Override
    public void detectAndSendChanges() {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {  /* ported stub */ }

    protected void handleSyncWidget() {
        if (!waitToRemoved.isEmpty()) {
            synchronized (waitToRemoved) {
                waitToRemoved.forEach(this::removeWidget);
                waitToRemoved.clear();
            }
        }
        if (!waitToAdded.isEmpty()) {
            synchronized (waitToAdded) {
                waitToAdded.forEach(this::addWidget);
                waitToAdded.clear();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void drawWidgetsForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    protected void drawWidgetsBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseMoved(double mouseX, double mouseY) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public boolean charTyped(char codePoint, int modifiers) { return false; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {  /* ported stub */ }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        if (id == 1) {
            int widgetIndex = buffer.readVarInt();
            int widgetUpdateId = buffer.readVarInt();
            Widget widget;
            if (widgetIndex < widgets.size()) {
                widget = widgets.get(widgetIndex);
            } else {
                synchronized (waitToAdded) {
                    widget = waitToAdded.get(widgets.size() - widgetIndex);
                }
            }
            widget.handleClientAction(widgetUpdateId, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        for (Widget widget : widgets) {
            widget.onScreenSizeUpdate(screenWidth, screenHeight);
        }
    }

    private class WidgetGroupUIAccess implements WidgetUIAccess {

        @Override
        public boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate) {
            WidgetUIAccess uiAccess = WidgetGroup.this.uiAccess;
            if (uiAccess != null) {
                return uiAccess.attemptMergeStack(itemStack, fromContainer, simulate);
            }
            return false;
        }

        @Override
        public void writeClientAction(Widget widget, int updateId, Consumer<RegistryFriendlyByteBuf> dataWriter) {
            WidgetGroup.this.writeClientAction(1, buffer -> {
                buffer.writeVarInt(widgets.indexOf(widget));
                buffer.writeVarInt(updateId);
                dataWriter.accept(buffer);
            });
        }

        @Override
        public void writeUpdateInfo(Widget widget, int updateId, Consumer<RegistryFriendlyByteBuf> dataWriter) {
            WidgetGroup.this.writeUpdateInfo(1, buffer -> {
                buffer.writeVarInt(widgets.indexOf(widget));
                buffer.writeVarInt(updateId);
                dataWriter.accept(buffer);
            });
        }

    }

    // *********** for jei rei ************* //

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Rect2i> getGuiExtraAreas(Rect2i guiRect, List<Rect2i> list) {
        for (Widget widget : widgets) {
            list = widget.getGuiExtraAreas(guiRect, list);
        }
        return super.getGuiExtraAreas(guiRect, list);
    }

    // *********** IConfigurableWidget ************* //

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        IConfigurableWidgetGroup.super.buildConfigurator(father);
        addWidgetsConfigurator(father);
    }

    protected void addWidgetsConfigurator(ConfiguratorGroup father) {  /* ported stub */ }

    @Override
    public boolean canWidgetAccepted(IConfigurableWidget widget) { return false; /* ported stub */ }

    @Override
    public void acceptWidget(IConfigurableWidget widget) {
        addWidget(widget.widget());
    }

    @Override
    public void onWidgetRemoved(IConfigurableWidget widget) {
        removeWidget(widget.widget());
    }

    @Override
    public CompoundTag serializeInnerNBT(HolderLookup.Provider provider) {
        CompoundTag tag = IConfigurableWidgetGroup.super.serializeInnerNBT(provider);
        var children = new ListTag();
        for (Widget widget : widgets) {
            if (widget instanceof IConfigurableWidget child && child.isLDLRegister()) {
                children.add(child.serializeWrapper(provider));
            }
        }
        tag.put("children", children);
        return tag;
    }

    @Override
    public void deserializeInnerNBT(HolderLookup.Provider provider, CompoundTag nbt) {  /* ported stub */ }
}
