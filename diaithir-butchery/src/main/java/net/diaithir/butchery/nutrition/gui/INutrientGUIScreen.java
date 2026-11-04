package net.diaithir.butchery.nutrition.gui;

import net.minecraft.client.Minecraft;

public interface INutrientGUIScreen {

    public int getWidth();
    public int getHeight();
    public ModWidget addModWidget(ModWidget modWidget);

    public Minecraft getMinecraft();
}
