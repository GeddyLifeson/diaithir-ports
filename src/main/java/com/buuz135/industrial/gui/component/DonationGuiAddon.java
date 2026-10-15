package com.buuz135.industrial.gui.component;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.renderer.RenderPipelines;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.client.screen.container.BasicAddonScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DonationGuiAddon extends BasicScreenAddon {

    private boolean open;

    public DonationGuiAddon(int posX, int posY) {
        super(posX, posY);
        this.open = false;
    }

    @Override
    public int getXSize() {
        return 22;
    }

    @Override
    public int getYSize() {
        return 24;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphicsExtractor guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        var rl = Identifier.fromNamespaceAndPath("titanium", "textures/gui/background_alt.png");
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, rl, getPosX() + guiX, getPosY() + guiY, 0, 0, 22, 22, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, rl, getPosX() + guiX, getPosY() + guiY + 22, 0, 81, 22, 3, 256, 256);

        var rlDonate = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/donate.png");
        guiGraphics.blit(rlDonate, getPosX() + guiX + 5, getPosY() + guiY + 5, 0, 0, 15, 15, 15, 15);
    }

    @Override
    public void drawForegroundLayer(GuiGraphicsExtractor guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public List<Component> getTooltipLines() {
        List<Component> components = new ArrayList<>();
        components.add(Component.literal(Component.translatable("text.industrialforegoing.donation.consider").getString()).withStyle(ChatFormatting.GOLD));
        components.add(Component.literal(Component.translatable("text.industrialforegoing.donation.author_of").getString()).withStyle(ChatFormatting.GOLD));
        IndustrialForegoing.OWN_MODS_LOADED.forEach(string -> components.add(Component.literal("- " + string).withStyle(ChatFormatting.WHITE)));
        components.add(Component.empty());
        components.add(Component.literal(Component.translatable("text.industrialforegoing.donation.click_for").getString()).withStyle(ChatFormatting.GRAY));
        return components;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof BasicAddonScreen basicAddonScreen) {
            if (isMouseOver(event.x() - basicAddonScreen.getGuiLeft(), event.y() - basicAddonScreen.getGuiTop())) {
                ConfirmLinkScreen.confirmLink(Minecraft.getInstance().screen, "https://github.com/sponsors/Buuz135").onPress(null);
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
