package com.lowdragmc.lowdraglib.gui.compass;

import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.FileUtility;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib.utils.XmlUtils;
import lombok.Getter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.io.input.ReaderInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static com.lowdragmc.lowdraglib.gui.compass.CompassManager.*;

/**
 * @author KilaBash
 * @date 2023/7/26
 * @implNote CompassView
 */
public class CompassView extends WidgetGroup {
    public static final int LIST_WIDTH = 150;

    public final String modID;
    public final ICompassUIConfig config;

    @Getter
    protected final Map<Identifier, CompassSection> sections = new LinkedHashMap<>();
    protected boolean editMode = false;
    protected WidgetGroup editModeWidget;
    protected DraggableScrollableWidgetGroup sectionList;
    protected WidgetGroup mainView;
    //runtime
    @Nullable
    protected Identifier openedSection, openedNode;
    @Nullable
    protected CompassSection selectedSection;
    protected Set<CompassSection> removedSections = new HashSet<>();

    public CompassView(String modID) {
        super(0, 0, 10, 10);
        setClientSideWidget();
        this.modID = modID;
        if (LDLib.isClient()) {
            this.config = INSTANCE.getUIConfig(modID);
        } else {
            this.config = null;
        }
    }

    public CompassView(CompassNode compassNode) {
        this(compassNode.section.sectionName.getNamespace());
        if (LDLib.isClient()) {
            this.openedSection = compassNode.section.getSectionName();
            this.openedNode = compassNode.getNodeName();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {  /* ported stub */ }

    @NotNull
    private SelectableWidgetGroup createSelectable(CompassSection section, int y) { return null; /* ported stub */ }

    protected void saveSections() {  /* ported stub */ }

    protected void setEditMode(boolean editMode) {  /* ported stub */ }

    protected TreeBuilder.Menu createMenu() { return null; /* ported stub */ }

    protected void createTextureMenu(TreeBuilder.Menu menu, String title, IGuiTexture initial, IGuiTexture defaultTexture, Consumer<IGuiTexture> consumer) {
        menu.branch(initial, title, m -> m
                .leaf(defaultTexture == initial ? defaultTexture : IGuiTexture.EMPTY, "ldlib.gui.compass.default", () -> consumer.accept(defaultTexture))
                .leaf((defaultTexture != initial && initial instanceof ResourceTexture) ? initial : IGuiTexture.EMPTY, "ldlib.gui.editor.register.texture.resource_texture", () -> DialogWidget.showStringEditorDialog(this, title, "ldlib:textures/gui/icon.png", Identifier::isValidPath, s -> {
                    if (s != null && LDLib.isValidResourceLocation(s)) {
                        consumer.accept(new ResourceTexture(s));
                    }
                }))
                .leaf((defaultTexture != initial && initial instanceof ItemStackTexture) ? initial : IGuiTexture.EMPTY, "ldlib.gui.editor.register.texture.item_texture", () -> DialogWidget.showItemSelector(this, title, ItemStack.EMPTY, item -> {
                    if (item != null && item != Items.AIR) {
                        consumer.accept(new ItemStackTexture(item));
                    }
                }))
                .leaf((defaultTexture != initial && initial instanceof ShaderTexture) ? initial : IGuiTexture.EMPTY, "ldlib.gui.editor.register.texture.shader_texture", () -> DialogWidget.showStringEditorDialog(this, title, "ldlib:compass_node", Identifier::isValidPath, s -> {
                    if (s != null && LDLib.isValidResourceLocation(s)) {
                        consumer.accept(ShaderTexture.createShader(Identifier.parse(s)));
                    }
                })));
    }

    private void adjustOrder(int y, boolean moveUp) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public void openSection(CompassSection section) {
        selectedSection = section;
        openedSection = section.sectionName;
        openedNode = null;
        mainView.clearAllWidgets();
        var sectionWidget = new CompassSectionWidget(this, section);
        mainView.addWidget(sectionWidget);
    }

    @OnlyIn(Dist.CLIENT)
    public void openNodeContent(CompassNode node) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    private void initCompass() {
        if (INSTANCE.devMode) {
            INSTANCE.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        }
        sections.clear();
        var sectionList = INSTANCE.sections.getOrDefault(modID, Collections.emptyMap());
        sectionList.values().stream().sorted(Comparator.comparingInt(a -> a.priority)).forEach(section -> {
            sections.put(section.sectionName, section);
        });
    }

}
