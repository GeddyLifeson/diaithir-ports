package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.editor.data.resource.Resource;
import com.lowdragmc.lowdraglib.gui.editor.ui.resource.ResourceContainer;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib.utils.interpolate.Eases;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/12/2
 * @implNote ResourcePanel
 */
public class ResourcePanel extends WidgetGroup {
    public static final int HEIGHT = 100;

    @Getter
    protected Editor editor;
    protected ButtonWidget buttonHide;
    protected TabContainer tabContainer;
    @Getter
    @Nullable
    protected Resources resources;
    @Getter
    protected boolean isShow = true;
    @Getter
    protected Map<Resource, ResourceContainer> containerMap = new HashMap<>();


    public ResourcePanel(Editor editor) {
        super(0, editor.getSize().height - HEIGHT, editor.getSize().getWidth() - ConfigPanel.WIDTH, HEIGHT);
        setClientSideWidget();
        this.editor = editor;
    }

    private void dispose() {
        if (resources != null) {
            resources.dispose();
        }
    }

    @Override
    public void setGui(ModularUI gui) {  /* ported stub */ }

    @Override
    public void initWidget() {  /* ported stub */ }

    public void hide() {  /* ported stub */ }

    public void show() {  /* ported stub */ }

    public void clear() {
        tabContainer.clearAllWidgets();
        containerMap.clear();
    }

    public void loadResource(Resources resources, boolean merge) {  /* ported stub */ }

    public void rebuildResource(String resourceName) {
        if (resources != null) {
            var resource = resources.resources.get(resourceName);
            if (resource != null) {
                var container = containerMap.get(resource);
                if (container != null) {
                    container.reBuild();
                }
            }
        }
    }

}
