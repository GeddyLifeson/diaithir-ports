package com.lowdragmc.lowdraglib.gui.compass.component;

import com.lowdragmc.lowdraglib.gui.compass.ILayoutComponent;
import com.lowdragmc.lowdraglib.gui.compass.LayoutPageWidget;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.json.IGuiTextureTypeAdapter;
import com.lowdragmc.lowdraglib.json.SimpleIGuiTextureJsonUtils;
import com.lowdragmc.lowdraglib.utils.XmlUtils;
import lombok.NoArgsConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.w3c.dom.Element;

import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2022/9/4
 * @implNote TextureComponent
 */
@NoArgsConstructor
public class ImageComponent extends TextBoxComponent {

    protected int width = 50;
    protected int height = 50;
    protected float u0 = 0, v0 = 0;
    protected float u1 = 1, v1 = 1;
    protected IGuiTexture guiTexture = new ColorBorderTexture(-1, -1);

    @Override
    public ILayoutComponent fromXml(Element element) { return null; /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        var imageWidget = new ImageWidget(0, 0, width, height, guiTexture);
        if (this.hoverInfo != null) {
            imageWidget.setHoverTooltips(hoverInfo);
        }
        currentPage.addStreamWidget(wrapper(imageWidget));
        currentPage.addOffsetSpace(3);
        return super.addWidgets(currentPage);
    }
}
