package com.lowdragmc.lowdraglib.gui.compass;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.LDLib;

import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.FileUtility;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Position;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.Pair;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/8/26
 * @implNote BookTabGroup
 */
public class CompassSectionWidget extends WidgetGroup {

    protected enum Mode {
        CURSOR(Icons.CURSOR),
        MOVE(Icons.MOVE),
        LINK(Icons.LINK);
        final IGuiTexture icon;

        Mode(IGuiTexture icon) {
            this.icon = icon;
        }
    }

    protected final CompassView compassView;
    protected final CompassSection section;
    protected float xOffset, yOffset;
    protected float scale = 1;
    protected double lastMouseX, lastMouseY;
    protected boolean isDragging = false;
    protected boolean editMode = false;
    protected WidgetGroup editModeWidget;
    protected Mode mode = Mode.CURSOR;
    protected int gridWidth = 50;
    protected boolean magnetic = true;
    @Nullable
    protected CompassNode selectedNode = null;
    protected Position originPosition = Position.ORIGIN;
    protected Pair<CompassNode, CompassNode> selectedLink = null;
    protected Set<CompassNode> removedNodes = new HashSet<>();

    public CompassSectionWidget(CompassView compassView, CompassSection section) {
        super(0, 0, compassView.getSize().width - CompassView.LIST_WIDTH, compassView.getSize().height);
        this.compassView = compassView;
        this.section = section;
        this.resetFitScale();
        // Reset View
        addWidget(new ButtonWidget(10, 10, 20, 20,
                new GuiTextureGroup(ColorPattern.T_GRAY.rectTexture(), Icons.ROTATION),
                cd -> resetFitScale()).setHoverTooltips(Component.translatable("ldlib.gui.compass.reset_view")));

        // Edit Mode
        if (CompassManager.INSTANCE.devMode) {
            addWidget(new SwitchWidget(40, 10, 20, 20, (cd, isPressed) -> setEditMode(isPressed))
                    .setSupplier(() -> editMode)
                    .setTexture(new GuiTextureGroup(ColorPattern.T_GRAY.rectTexture(), Icons.EDIT_OFF),
                            new GuiTextureGroup(ColorPattern.T_CYAN.rectTexture(), Icons.EDIT_ON))
                    .setHoverTooltips(Component.translatable("ldlib.gui.compass.edit_mode")));
        }

        addWidget(editModeWidget = new WidgetGroup(0, 0, getSize().width, getSize().height));
        editModeWidget.setVisible(false);
        editModeWidget.setActive(false);

        editModeWidget.addWidget(new ButtonWidget(40, 30, 20, 20,
                new GuiTextureGroup(ColorPattern.T_RED.rectTexture(), Icons.SAVE), cd -> saveSection())
                .setHoverTooltips(Component.translatable("ldlib.gui.editor.menu.save")));

        TextBoxWidget textBox;;
        editModeWidget.addWidget(textBox = new TextBoxWidget(getSize().width - 200, 10, 200,
                List.of("ldlib.gui.compass.mode.%s.tooltip".formatted(mode.name().toLowerCase())))
                .setFontColor(-1)
                .setShadow(true));

        for (Mode mode : Mode.values()) {
            editModeWidget.addWidget(new ImageWidget(62 + 16 * mode.ordinal(), 32, 16, 16, () -> this.mode == mode ? ResourceBorderTexture.SELECTED.copy().setColor(ColorPattern.RED.color) : IGuiTexture.EMPTY));
            editModeWidget.addWidget(new ButtonWidget(62 + 16 * mode.ordinal(), 32, 16, 16, mode.icon, cd -> {
                switchMode(mode);
                textBox.setContent(List.of("ldlib.gui.compass.mode.%s.tooltip".formatted(mode.name().toLowerCase())));
            })
                    .setHoverTexture(new GuiTextureGroup(ResourceBorderTexture.SELECTED.copy().setColor(ColorPattern.T_RED.color), mode.icon))
                    .setHoverTooltips(Component.translatable("ldlib.gui.compass.mode." + mode.name().toLowerCase())));
        }

    }

    private void saveSection() {
        var path = new File(LDLib.getLDLibDir(), "assets/%s/compass/nodes".formatted(section.getSectionName().getNamespace()));
        if (!path.isDirectory()) {
            if (path.mkdirs()) {
                LDLib.LOGGER.info("Created directory %s".formatted(path));
            } else {
                LDLib.LOGGER.error("Failed to create directory %s".formatted(path));
                return;
            }
        }

        for (CompassNode removedNode : removedNodes) {
            var file = new File(path, removedNode.getNodeName().getPath() + ".json");
            if (file.exists()) {
                if (!file.delete()) {
                    LDLib.LOGGER.error("Failed to delete file %s".formatted(file));
                }
            }
        }
        removedNodes.clear();

        for (CompassNode node : section.nodes.values()) {
            var file = new File(path, node.getNodeName().getPath() + ".json");
            FileUtility.saveJson(file, node.updateJson());
        }

        DialogWidget.showCheckBox(compassView, "ldlib.gui.compass.save_success", "ldlib.gui.compass.save_success.desc", shouldOpen -> {
            if (shouldOpen) {
                Util.getPlatform().openFile(path);
            }
        });

        CompassManager.INSTANCE.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
    }

    protected void setEditMode(boolean editMode) {
        this.editMode = editMode;
        this.removedNodes.clear();
        editModeWidget.setVisible(editMode);
        editModeWidget.setActive(editMode);
    }

    protected void switchMode(Mode mode) {
        this.mode = mode;
        this.selectedNode = null;
        this.selectedLink = null;
    }

    protected void resetFitScale() {
        int minX, minY, maxX, maxY;
        if (this.section.nodes.isEmpty()) {
            this.xOffset = 0;
            this.yOffset = 0;
            this.scale = 1;
            return;
        }
        minX = minY = Integer.MAX_VALUE;
        maxX = maxY = Integer.MIN_VALUE;
        for (CompassNode node : this.section.nodes.values()) {
            Position position = node.getPosition();
            minX = Math.min(minX, position.x - node.size);
            minY = Math.min(minY, position.y - node.size);
            maxX = Math.max(maxX, position.x + node.size);
            maxY = Math.max(maxY, position.y + node.size);
        }
        this.xOffset = minX;
        this.yOffset = minY;
        var scaleWidth = (float) getSize().width / (maxX - minX);
        var scaleHeight = (float) getSize().height / (maxY - minY);
        this.scale = Math.min(scaleWidth, scaleHeight);
        if (scale < 0.5f) {
            this.scale = 0.5f;
        }
        this.xOffset -= (getSize().width / scale - (maxX - minX)) / 2;
        this.yOffset -= (getSize().height / scale - (maxY - minY)) / 2;
    }

    @Nullable
    protected TreeBuilder.Menu createMenu(double mouseX, double mouseY) { return null; /* ported stub */ }

    protected void createTextureMenu(TreeBuilder.Menu menu, String title, IGuiTexture initial, IGuiTexture defaultTexture, Consumer<IGuiTexture> consumer) {
        menu.branch(initial, title, m -> m
                .leaf(defaultTexture == initial ? defaultTexture : IGuiTexture.EMPTY, "ldlib.gui.compass.default", () -> consumer.accept(defaultTexture))
                .leaf((defaultTexture != initial && initial instanceof ResourceTexture) ? initial : IGuiTexture.EMPTY, "ldlib.gui.editor.register.texture.resource_texture", () -> DialogWidget.showStringEditorDialog(compassView, title, "ldlib:textures/gui/icon.png", Identifier::isValidPath, s -> {
                    if (s != null && LDLib.isValidResourceLocation(s)) {
                        consumer.accept(new ResourceTexture(s));
                    }
                }))
                .leaf((defaultTexture != initial && initial instanceof ItemStackTexture) ? initial : IGuiTexture.EMPTY, "ldlib.gui.editor.register.texture.item_texture", () -> DialogWidget.showItemSelector(compassView, title, ItemStack.EMPTY, item -> {
                    if (item != null && item != Items.AIR) {
                        consumer.accept(new ItemStackTexture(item));
                    }
                }))
                .leaf((defaultTexture != initial && initial instanceof ShaderTexture) ? initial : IGuiTexture.EMPTY, "ldlib.gui.editor.register.texture.shader_texture", () -> DialogWidget.showStringEditorDialog(compassView, title, "ldlib:compass_node", Identifier::isValidPath, s -> {
                    if (s != null && LDLib.isValidResourceLocation(s)) {
                        consumer.accept(ShaderTexture.createShader(Identifier.parse(s)));
                    }
                })));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (isMouseOverElement(mouseX, mouseY)) {
            if (button == 0) {
                int newMouseX = (int) ((mouseX - this.getPosition().x) / scale + xOffset);
                int newMouseY = (int) ((mouseY - this.getPosition().y) / scale + yOffset);
                if (editMode) {
                    for (CompassNode node : this.section.nodes.values()) {
                        if (isNodeOver(node, newMouseX, newMouseY)) {
                            switch (mode) {
                                case CURSOR -> {
                                    selectedNode = node;
                                    continue;
                                }
                                case MOVE -> {
                                    selectedNode = node;
                                    originPosition = node.getPosition();
                                }
                                case LINK -> {
                                    selectedNode = node;
                                }
                            }
                            return true;
                        }
                    }
                    if (mode == Mode.LINK) {
                        for (CompassNode node : this.section.nodes.values()) {
                            for (CompassNode preNode : node.preNodes) {
                                var vec = new Vector2f(node.getPosition().x - preNode.getPosition().x, node.getPosition().y - preNode.getPosition().y);
                                var mid = new Vector2f((node.getPosition().x + preNode.getPosition().x) / 2f, (node.getPosition().y + preNode.getPosition().y) / 2f);
                                var mouseVec = new Vector2f(newMouseX, newMouseY).sub(mid);
                                var project = new Vector2f(0f, 0f);
                                float l = vec.lengthSquared();
                                if (l != 0.0D) {
                                    project.set(vec).mul(mouseVec.dot(vec) / l);
                                }
                                if (project.lengthSquared() < vec.lengthSquared() / 4f) {
                                    var dist = project.sub(mouseVec).length();
                                    if (dist < 10) {
                                        selectedLink = Pair.of(preNode, node);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                isDragging = true;
            } else if (button == 1) {
                if (editMode) {
                    var menu = createMenu(mouseX, mouseY);
                    if (menu != null) {
                        compassView.waitToAdded(new MenuWidget<>((int) mouseX, (int) mouseY, 14, menu.build())
                                .setNodeTexture(MenuWidget.NODE_TEXTURE)
                                .setLeafTexture(MenuWidget.LEAF_TEXTURE)
                                .setNodeHoverTexture(MenuWidget.NODE_HOVER_TEXTURE)
                                .setCrossLinePredicate(TreeBuilder.Menu::isCrossLine)
                                .setKeyIconSupplier(TreeBuilder.Menu::getIcon)
                                .setKeyNameSupplier(TreeBuilder.Menu::getName)
                                .setOnNodeClicked(TreeBuilder.Menu::handle)
                                .setBackground(MenuWidget.BACKGROUND));
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        var lastSelectedNode = selectedNode;
        isDragging = false;
        if (!editMode || mode != Mode.CURSOR) {
            selectedNode = null;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            int newMouseX = (int) ((mouseX - this.getPosition().x) / scale + xOffset);
            int newMouseY = (int) ((mouseY - this.getPosition().y) / scale + yOffset);
            if (editMode) {
                if (mode == Mode.LINK && lastSelectedNode != null) {
                    for (CompassNode node : this.section.nodes.values()) {
                        if (lastSelectedNode != node && isNodeOver(node, newMouseX, newMouseY)) {
                            node.preNodes.add(lastSelectedNode);
                            lastSelectedNode.childNodes.add(node);
                            return true;
                        }
                    }
                }
            } else {
                for (CompassNode node : this.section.nodes.values()) {
                    if (isNodeOver(node, newMouseX, newMouseY)) {
                        compassView.openNodeContent(node);
                        return true;
                    }
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            xOffset += (float) (lastMouseX - mouseX) / scale;
            yOffset += (float) (lastMouseY - mouseY) / scale;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
        if (selectedNode != null && editMode) {
            if (mode == Mode.MOVE) {
                var x = originPosition.x + (int) ((mouseX - lastMouseX) / scale);
                var y = originPosition.y + (int) ((mouseY - lastMouseY) / scale);
                if (magnetic && !isShiftDown()) {
                    x -= x % 10;
                    y -= y % 10;
                }
                selectedNode.setPosition(new Position(x, y));
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            var newScale = (float) Mth.clamp(scale + scrollY * 0.1f, 0.1f, 10f);
            if (newScale != scale) {
                xOffset += (float) (mouseX - this.getPosition().x) / scale - (float) (mouseX - this.getPosition().x) / newScale;
                yOffset += (float) (mouseY - this.getPosition().y) / scale - (float) (mouseY - this.getPosition().y) / newScale;
                scale = newScale;
            }
        }
        return super.mouseWheelMove(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            int newMouseX = (int) ((mouseX - this.getPosition().x) / scale + xOffset);
            int newMouseY = (int) ((mouseY - this.getPosition().y) / scale + yOffset);
            for (CompassNode node : this.section.nodes.values()) {
                if (isNodeOver(node, newMouseX, newMouseY)) {
                    gui.getModularUIGui().setHoverTooltip(List.of(node.getChatComponent()), ItemStack.EMPTY, null, null);
                }
            }
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    protected boolean isNodeOver(CompassNode node, int newMouseX, int newMouseY) {
        return isMouseOver(node.getPosition().x - node.size / 2, node.getPosition().y - node.size / 2, node.size, node.size, newMouseX, newMouseY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    protected void drawNode(GuiGraphics graphics, int mouseX, int mouseY, CompassNode node) {  /* ported stub */ }


    @OnlyIn(Dist.CLIENT)
    protected void drawChildLines(GuiGraphics graphics, CompassNode node) {  /* ported stub */ }

}
