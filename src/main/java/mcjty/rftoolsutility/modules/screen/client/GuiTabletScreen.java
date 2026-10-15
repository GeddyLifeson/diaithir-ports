package mcjty.rftoolsutility.modules.screen.client;

import net.minecraft.client.input.MouseButtonEvent;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenContainer;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.positional;

public class GuiTabletScreen extends GenericGuiContainer<ScreenTileEntity, ScreenContainer> {

    public static final int WIDTH = 200;
    public static final int HEIGHT = 190;

    public GuiTabletScreen(ScreenContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, /* @todo 1.14 */ ManualEntry.EMPTY);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    public static void register(RegisterMenuScreensEvent event) {
        MenuScreens.ScreenConstructor<ScreenContainer, GuiTabletScreen> factory = (container, inventory, title) -> {
            BlockEntity te = container.getBe();
            return Tools.safeMap(te, (ScreenTileEntity tile) -> new GuiTabletScreen(container, inventory, Component.literal("Title")), "Invalid tile entity!");
        };
        event.register(ScreenModule.CONTAINER_SCREEN_REMOTE.get(), factory);
        event.register(ScreenModule.CONTAINER_SCREEN_REMOTE_CREATIVE.get(), factory);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = positional();
        toplevel.bounds(leftPos, topPos, imageWidth, imageHeight);
        window = new Window(this, toplevel);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        //            AbstractGui.fill(100, 30, 250, 180, 0xff333333);
        double dx = 1.0 - ((event.x() - 102) / 60.0);
        double dy = 1.0 - ((event.y() - 32) / 60.0);
        ScreenTileEntity tileEntity = getBE();
        ScreenTileEntity.ModuleRaytraceResult result = tileEntity.getHitModule(dx, dy, 0, Direction.NORTH, Direction.NORTH, 1);
        if (result != null) {
            tileEntity.hitScreenClient(result);
        }
        return false;
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        ScreenTileEntity tileEntity = getBE();
        tileEntity.tickMe();
//        super.render(mouseX, mouseY, partialTicks);
    }
}

