package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.editor.configurator.Configurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurable;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote ConfigPanel
 */
public class ConfigPanel extends WidgetGroup {
    public static final int WIDTH = 252;
    public static class Tab {
        public static final Tab WIDGET = createTab(Icons.WIDGET_SETTING, Component.translatable("ldlib.gui.editor.config_panel.tabs.widget"));
        public static final Tab RESOURCE = createTab(Icons.RESOURCE_SETTING, Component.translatable("ldlib.gui.editor.config_panel.tabs.resource"));

        public final ResourceTexture icon;
        public final Component tooltip;
        public final Consumer<ConfiguratorGroup> configurable;

        private Tab(ResourceTexture icon, Component tooltip, Consumer<ConfiguratorGroup> configurable) {
            this.icon = icon;
            this.tooltip = tooltip;
            this.configurable= configurable;
        }

        public static Tab createTab(ResourceTexture icon, Component tooltip) {
            return createTab(icon, tooltip, father -> {});
        }

        public static Tab createTab(ResourceTexture icon, Component tooltip, Consumer<ConfiguratorGroup> configurable) {
            return new Tab(icon, tooltip, configurable);
        }
    }

    @Getter
    protected final Editor editor;
    @Getter
    protected final Map<Tab, IConfigurable> focus = new HashMap<>();
    protected final Map<Tab, DraggableScrollableWidgetGroup> configuratorGroup = new HashMap<>();
    protected final Map<Tab, List<Configurator>> configurators = new HashMap<>();

    protected ImageWidget tabBackground;
    protected TabContainer tabContainer;
    @Getter
    protected HsbColorWidget palette;

    public ConfigPanel(Editor editor, List<Tab> tabs) {
        super(editor.getSize().getWidth() - WIDTH, 0, WIDTH, editor.getSize().height);
        setClientSideWidget();
        this.editor = editor;
        setBackground(ColorPattern.BLACK.rectTexture());
        addWidget(new ImageWidget(0, 10, WIDTH, 10, new TextTexture("ldlib.gui.editor.configurator").setWidth(202)));
        addWidget(tabBackground = new ImageWidget(-20, 30, 20, 2 * 20, ColorPattern.BLACK.rectTexture().setLeftRadius(8)));
        addWidget(tabContainer = new TabContainer(0, 0, WIDTH, editor.getSize().height));
        reloadTabs(tabs);
    }

    public ConfigPanel(Editor editor) {
        this(editor, List.of(Tab.WIDGET, Tab.RESOURCE));
    }

    public void reloadTabs(List<Tab> tabs) {  /* ported stub */ }

    public void clearAllConfigurators() {
        for (var tab : this.configurators.keySet()) {
            clearAllConfigurators(tab);
        }
    }

    public void clearAllConfigurators(Tab tab) {
        this.focus.remove(tab);
        configuratorGroup.get(tab).clearAllWidgets();
        configurators.get(tab).clear();
    }

    public void openConfigurator(Tab tab, IConfigurable configurable) {  /* ported stub */ }

    public void onConfiguratorChangeUpdate(Tab tab, Configurator configurator) {  /* ported stub */ }

    public void switchTag(Tab tab) {
        tabContainer.switchTag(configuratorGroup.get(tab));
    }
    
    public void computeLayout(Tab tab) {  /* ported stub */ }
}
