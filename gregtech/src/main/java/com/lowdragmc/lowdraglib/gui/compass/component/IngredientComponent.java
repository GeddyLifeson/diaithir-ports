package com.lowdragmc.lowdraglib.gui.compass.component;

import com.lowdragmc.lowdraglib.gui.compass.ILayoutComponent;
import com.lowdragmc.lowdraglib.gui.compass.LayoutPageWidget;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.XmlUtils;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/10/23
 * @implNote SlotComponent
 */
public class IngredientComponent extends AbstractComponent {
    List<Object> ingredients = new ArrayList<>();

    @Override
    public ILayoutComponent fromXml(Element element) {
        super.fromXml(element);
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element e) {
                if (e.getNodeName().equals("item")) {
                    var ingredient = XmlUtils.getIngredient(e);
                    ingredients.add(ingredient);
                }
                if (e.getNodeName().equals("fluid")) {
                    var fluidStack = XmlUtils.getFluidStack(e);
                    ingredients.add(fluidStack);
                }
            }
        }
        return this;
    }

    @Override
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) { return null; /* ported stub */ }
}
