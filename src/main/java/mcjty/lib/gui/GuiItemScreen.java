package mcjty.lib.gui;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.input.KeyEvent;

import mcjty.lib.client.GuiTools;
import mcjty.lib.varia.ComponentFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class can be used by items that want to show a GUI.
 * It supports the side window, the window system in general as well as tooltips
 */
public abstract class GuiItemScreen extends Screen {
    protected Window window;
    protected int xSize;
    protected int ySize;
    protected int guiLeft;
    protected int guiTop;
    private final ManualEntry manual;

    private GuiSideWindow sideWindow;

    public GuiItemScreen(int xSize, int ySize, ManualEntry manualEntry) {
        super(ComponentFactory.literal("todo")); // @todo 1.14
        this.xSize = xSize;
        this.ySize = ySize;
        sideWindow = new GuiSideWindow(manualEntry.manual(), manualEntry.entry(), manualEntry.page());
        this.manual = manualEntry;
    }

    public void setWindowDimensions(int x, int y) {
        this.xSize = x;
        this.ySize = y;
        sideWindow = new GuiSideWindow(manual.manual(), manual.entry(), manual.page());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        guiLeft = (this.width - xSize) / 2;
        guiTop = (this.height - ySize) / 2;
        sideWindow.initGui(getMinecraft(), this, guiLeft, guiTop, xSize, ySize);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean rc = super.mouseClicked(event, doubleClick);
        window.mouseClicked(event, doubleClick);
        sideWindow.getWindow().mouseClicked(event, doubleClick);
        return rc;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double scaledX, double scaledY) {
        boolean rc = super.mouseDragged(event, scaledX, scaledY);
        window.mouseDragged(event.x(), event.y(), event.button());
        sideWindow.getWindow().mouseDragged(event.x(), event.y(), event.button());
        return rc;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        boolean rc = super.mouseScrolled(x, y, dx, dy);
        window.mouseScrolled(x, y, dx, dy);
        sideWindow.getWindow().mouseScrolled(x, y, dx, dy);
        return rc;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean rc = super.mouseReleased(event);
        window.mouseReleased(event);
        sideWindow.getWindow().mouseReleased(event);
        return rc;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean rc = super.keyPressed(event);
//        window.keyTyped(typedChar, event.key());
        window.keyTyped(event.key(), event.scancode());
        return rc;
    }

    public void drawWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        window.draw(graphics);
        sideWindow.getWindow().draw(graphics);
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = GuiTools.getRelativeX(this);
            int y = GuiTools.getRelativeY(this);
            // @todo check on 1.16
            List<FormattedText> properties = tooltips.stream().map(ComponentFactory::literal).collect(Collectors.toList());
            List<FormattedCharSequence> processors = Language.getInstance().getVisualOrder(properties);
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, processors, x-guiLeft, y-guiTop);
        }
        tooltips = sideWindow.getWindow().getTooltips();
        if (tooltips != null) {
            int x = GuiTools.getRelativeX(this);
            int y = GuiTools.getRelativeY(this);
            // @todo check on 1.16
            List<FormattedText> properties = tooltips.stream().map(ComponentFactory::literal).collect(Collectors.toList());
            List<FormattedCharSequence> processors = Language.getInstance().getVisualOrder(properties);
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, processors, x - guiLeft, y - guiTop);
        }
    }

    protected abstract void renderInternal(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick);

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractRenderState(graphics, pMouseX, pMouseY, pPartialTick);
        renderInternal(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
