package com.lowdragmc.lowdraglib.gui.compass.component.animation;

import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.XmlUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/29
 * @implNote InformationAction
 */
public class InformationAction extends Action {

    private final IGuiTexture guiTexture;
    private final List<Component> text = new ArrayList<>();

    public InformationAction(IGuiTexture guiTexture, List<Component> text) {
        this.guiTexture = guiTexture;
        this.text.addAll(text);
    }

    public InformationAction(Element element) {
        super(element);
        var u0 = XmlUtils.getAsFloat(element, "u0", 0);
        var v0 = XmlUtils.getAsFloat(element, "v0", 0);
        var u1 = XmlUtils.getAsFloat(element, "u1", 1);
        var v1 = XmlUtils.getAsFloat(element, "v1", 1);
        String type = XmlUtils.getAsString(element, "type", "");
        String url = XmlUtils.getAsString(element, "url", "");

        guiTexture = switch (type) {
            case "resource" -> new ResourceTexture(url).getSubTexture(u0, v0, u1, v1);
            case "item" -> new ItemStackTexture(BuiltInRegistries.ITEM.get(Identifier.parse(url)));
            case "shader" -> ShaderTexture.createShader(Identifier.parse(url));
            default -> IGuiTexture.EMPTY;
        };

        text.addAll(XmlUtils.getComponents(element, Style.EMPTY));
    }

    @Override
    public int getDuration() {
        return 20;
    }

    @Override
    public void performAction(AnimationFrame frame, CompassScene scene, boolean anima) {  /* ported stub */ }
}
